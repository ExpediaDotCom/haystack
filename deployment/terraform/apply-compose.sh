#!/bin/bash
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

#########################
# The command line help #
#########################
function display_help() {
    echo "Usage: $0 [option...] " >&2
    echo
    echo "   -a , --action                  choose the action for deploying haystack components. possible values: install-all | install-apps | uninstall-all | uninstall-apps. Default: install-all"
    echo "   -c , --cluster-type            choose the cluster-type settings for cluster. possible values: aws | local, default: local"
    echo "   -cn, --cluster-name            name of the cluster. must be unique for every cluster created in aws,default: haystack"
    echo "   -sb, --s3-bucket               name of the s3 bucket where the deployment state would be stored, its mandatory when the cluster type is aws"
    echo "   -af, --appvars-file-path       app configuration values which need to be passed to terraform in a tfvars file(required for aws deployment) eg : trends_version, traces_version, default:cluster/aws|local/variables.json "
    echo "   -if, --infravars-file-path     infrastructure configuration which need to be passed to terraform in a tfvars file(required for aws deployment) eg : s3_bucket_name, aws_vpc_id, default:cluster/aws|local/variables.json "
    echo "   -s , --skip-approval           skips interactive approval of deployment plan before applying,default = false"


    exit 1
}

while :
do
    case "$1" in
      -c | --cluster-type)
          if [ $# -ne 0 ]; then
            CLUSTER_TYPE="$2"
          fi
          shift 2
          ;;
      -a | --action)
          if [ $# -ne 0 ]; then
            ACTION="$2"
          fi
          shift 2
          ;;
       -cn | --cluster-name)
          if [ $# -ne 0 ]; then
            CLUSTER_NAME="$2"
          fi
          shift 2
          ;;
       -sb | --s3-bucket)
          if [ $# -ne 0 ]; then
            S3_BUCKET="$2"
          fi
          shift 2
          ;;
       -if | --infravars-file-path)
          if [ $# -ne 0 ]; then
            INFRA_VARS_FILE="$2"
          fi
          shift 2
          ;;
       -af | --appvars-file-path)
          if [ $# -ne 0 ]; then
            APP_VARS_FILE="$2"
          fi
          shift 2
          ;;
       -s | --skip-approval)
          if [ $# -ne 0 ]; then
            SKIP_APPROVAL="$2"
          fi
          shift 2
          ;;
      -h | --help)
          display_help  # Call your function
          exit 0
          ;;
      --) # End of all options
          shift
          break
          ;;
      -*)
          echo "Error: Unknown option: $1" >&2
          ## or call function display_help
          exit 1
          ;;
      *)  # No more options
          break
          ;;
    esac
done

function verifyArgs() {

 if [[ -z $ACTION ]]; then
   ACTION=install-all
 fi
  if [[ -z $CLUSTER_TYPE ]]; then
   CLUSTER_TYPE=local
 fi
 if [[ -z $APP_VARS_FILE ]]; then
   APP_VARS_FILE=$DIR/cluster/$CLUSTER_TYPE/apps/variables.json
 fi
 if [[ -z $CLUSTER_NAME ]]; then
   CLUSTER_NAME=haystack
 fi
 if [[ -z $INFRA_VARS_FILE ]]; then
   INFRA_VARS_FILE=$DIR/cluster/$CLUSTER_TYPE/infrastructure/variables.json
 fi
 if [[ (-z $S3_BUCKET)  && "$CLUSTER_TYPE" = "aws"  ]] ; then
   echo "flag --s3-bucket|-sb needs to be passed when cluster_type is aws"
   exit 1
 fi
 if [[ -z ${SKIP_APPROVAL} ]]; then
   SKIP_APPROVAL="false"
 fi

 if [ ! -f ${APP_VARS_FILE} ]; then
    echo "{}" >> ${APP_VARS_FILE}
 fi
 if [ ! -f ${INFRA_VARS_FILE} ]; then
    echo "{}" >> ${INFRA_VARS_FILE}
 fi

}

function setThirdPartySoftwareBasePath() {
 case "$(uname -s)" in
    Darwin)
      THIRD_PARTY_SOFTWARE_PATH=$DIR/third_party_softwares/mac/x64
      ;;
    Linux)
      THIRD_PARTY_SOFTWARE_PATH=$DIR/third_party_softwares/linux/x64
      ;;
 esac
}

function downloadThirdPartySoftwares() {

 setThirdPartySoftwareBasePath
 # variable for accessing third party softwares
 TERRAFORM=$THIRD_PARTY_SOFTWARE_PATH/terraform
 KUBECTL=$THIRD_PARTY_SOFTWARE_PATH/kubectl
 KOPS=$THIRD_PARTY_SOFTWARE_PATH/kops

 if [ ! -f $TERRAFORM ]|| [ ! -f $KUBECTL ]|| [ ! -f $KOPS ]; then
   $DIR/install-third-party-softwares.sh
 fi
}

function command_exists () {
    type "$1" &> /dev/null ;
}

function applyActionOnComponents() {
    case "$ACTION" in
       install-all)
          installInfrastructure
          installComponents
          echo "Congratulations! you've successfully created haystack infrastructure and deployed haystack apps"
          ;;
       install-apps)
          installComponents
          echo "Congratulations! you've successfully redeployed haystack apps"
          ;;
       uninstall-apps)
          uninstallComponents
          echo "Congratulations! you've successfully uninstalled haystack apps"
          ;;
       uninstall-all)
          uninstallInfrastructure
          echo "Congratulations! you've successfully and deleted haystack apps and destroyed haystack infrastructure"
          ;;
       *)
          echo "Error!!! Fail to understand the action type, see the help."
          display_help
          exit 1
          ;;
    esac
}

function uninstallComponents() {

 cd $DIR/cluster/$CLUSTER_TYPE/apps
 if [ "$SKIP_APPROVAL" = "true" ];then
   FORCE_FLAG="-force"
   else
    echo "$SKIP_APPROVAL"
 fi
    echo "Deleting haystack apps using terraform"
 case "$CLUSTER_TYPE" in
    aws)
        $TERRAFORM init -backend-config="bucket=$S3_BUCKET" -backend-config="key=terraform/$CLUSTER_NAME-apps"
        #setting the correct kubectl config for terraform
        AWS_DOMAIN_NAME=$(echo "var.aws_domain_name" | $TERRAFORM console -var-file=$APP_VARS_FILE)
        echo "setting kubectl context : $CLUSTER_NAME-k8s.$AWS_DOMAIN_NAME"
        $KOPS export kubecfg --name $CLUSTER_NAME-k8s.$AWS_DOMAIN_NAME  --state s3://$S3_BUCKET
        $TERRAFORM destroy $FORCE_FLAG -var-file=$APP_VARS_FILE -var haystack_cluster_name=$CLUSTER_NAME -var s3_bucket_name=$S3_BUCKET  -var kubectl_executable_name=$KUBECTL
        ;;

    local)
        $TERRAFORM init
        $TERRAFORM destroy $FORCE_FLAG -var-file=$APP_VARS_FILE -var kubectl_executable_name=$KUBECTL
        ;;
 esac
}

function uninstallInfrastructure() {
    cd $DIR/cluster/$CLUSTER_TYPE/infrastructure
    if [ "$SKIP_APPROVAL" = "true" ];then
        FORCE_FLAG="-force"
    else
        echo "$SKIP_APPROVAL"
    fi
    echo "Deleting haystack infrastructure using terraform"
    case "$CLUSTER_TYPE" in
        aws)
            $TERRAFORM init -backend-config="bucket=$S3_BUCKET" -backend-config="key=terraform/$CLUSTER_NAME-infrastructure"
            #not explicitly deleting the k8s addons module since we're anyways destroying the k8s cluster
            $TERRAFORM state rm module.k8s-addons
            $TERRAFORM destroy $FORCE_FLAG -var-file=$INFRA_VARS_FILE -var kubectl_executable_name=$KUBECTL -var kops_executable_name=$KOPS -var haystack_cluster_name=$CLUSTER_NAME -var s3_bucket_name=$S3_BUCKET
            ;;

        local)
            $TERRAFORM init
            $TERRAFORM destroy $FORCE_FLAG -var-file=$INFRA_VARS_FILE -var kubectl_executable_name=$KUBECTL
            ;;
    esac
}

function installInfrastructure() {

    cd $DIR/cluster/$CLUSTER_TYPE/infrastructure
    if [ "$SKIP_APPROVAL" = "true" ];then
        AUTO_APPROVE="-auto-approve"
        else
        echo "$SKIP_APPROVAL"
    fi
    echo "Creating haystack infrastructure using terraform "
    case "$CLUSTER_TYPE" in
        aws)
            $TERRAFORM init -backend-config="bucket=$S3_BUCKET" -backend-config="key=terraform/$CLUSTER_NAME-infrastructure"
            #setting the correct kubectl config for terraform
            AWS_DOMAIN_NAME=$(echo "var.aws_domain_name" | $TERRAFORM console -var-file=$INFRA_VARS_FILE)
            echo "setting kubectl context : $CLUSTER_NAME-k8s.$AWS_DOMAIN_NAME"
            $KOPS export kubecfg --name $CLUSTER_NAME-k8s.$AWS_DOMAIN_NAME  --state s3://$S3_BUCKET || true
            $TERRAFORM apply $AUTO_APPROVE -var-file=$INFRA_VARS_FILE -var kubectl_executable_name=$KUBECTL -var kops_executable_name=$KOPS -var haystack_cluster_name=$CLUSTER_NAME -var s3_bucket_name=$S3_BUCKET
            ;;

        local)
            $TERRAFORM init
            $TERRAFORM apply $AUTO_APPROVE -var-file=$INFRA_VARS_FILE -var kubectl_executable_name=$KUBECTL
            ;;
    esac
}

function installComponents() {

    cd $DIR/cluster/$CLUSTER_TYPE/apps
    if [ "$SKIP_APPROVAL" = "true" ];then
        AUTO_APPROVE="-auto-approve"
        else
        echo "$SKIP_APPROVAL"
    fi

    echo "deploying haystack-apps using terraform"

     case "$CLUSTER_TYPE" in
    aws)
        $TERRAFORM init -backend-config="bucket=$S3_BUCKET" -backend-config="key=terraform/$CLUSTER_NAME-apps"
        #setting the correct kubectl config for terraform
        AWS_DOMAIN_NAME=$(echo "var.aws_domain_name" | $TERRAFORM console -var-file=$APP_VARS_FILE)
        echo "setting kubectl context : $CLUSTER_NAME-k8s.$AWS_DOMAIN_NAME"
        $KOPS export kubecfg --name $CLUSTER_NAME-k8s.$AWS_DOMAIN_NAME  --state s3://$S3_BUCKET
        $TERRAFORM apply $AUTO_APPROVE -var-file=$APP_VARS_FILE -var haystack_cluster_name=$CLUSTER_NAME -var s3_bucket_name=$S3_BUCKET  -var kubectl_executable_name=$KUBECTL
        ;;

    local)
        $TERRAFORM init
        $TERRAFORM apply $AUTO_APPROVE -var-file=$INFRA_VARS_FILE -var kubectl_executable_name=$KUBECTL
        ;;
    esac
}

function verifyK8sCluster() {
  if [[ $CLUSTER_TYPE == 'local' ]]; then
    if command_exists minikube; then
        `minikube status > /tmp/minikube_status`
        if grep -q -i 'Running' /tmp/minikube_status; then
            echo "Congratulations! Minikube is found in running state!"
        else
             echo 'Minikube is not running, starting now...'
             minikube start
          fi
          rm -rf /tmp/minikube_status
    else
        echo "Minikube is not installed on local box, please setup minikube by following the instructions at https://kubernetes.io/docs/getting-started-guides/minikube"
        exit 1
    fi
  fi
}

# sanitize the arguments passed to the script, and set the defaults correctly
verifyArgs

# verify minikube is running in local mode
verifyK8sCluster

# download third party softwares like kubectl, gomplate, jq etc.
downloadThirdPartySoftwares

# install/delete the haystack components
applyActionOnComponents
