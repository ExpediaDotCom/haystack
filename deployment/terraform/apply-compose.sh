#!/bin/bash

set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

#########################
# The command line help #
#########################
function display_help() {
    echo "Usage: $0 [option...] " >&2
    echo
    echo "   -r     choose the action for deploying haystack components. possible values: install-all | install-apps | uninstall-all | uninstall-apps | delete-state. Default: install-all"
    echo "   -c     choose the cluster-type settings for cluster. possible values: aws | local, default: local"
    echo "   -n     name of the cluster. must be unique for every cluster created in aws,default: haystack"
    echo "   -b     name of the s3 bucket where the deployment state would be stored, its mandatory when the cluster type is aws"
    echo "   -o     app configuration values which need to be passed to terraform in a tfvars file(required for aws deployment) eg : trends_version, traces_version, default:cluster/aws|local/overrides.json "
    echo "   -i     infrastructure configuration which need to be passed to terraform in a tfvars file(required for aws deployment) eg : s3_bucket_name, aws_vpc_id, default:cluster/aws|local/overrides.json "
    echo "   -s     flag to skip interactive approval of deployment plan before applying"
    echo "   -t     flag to fetch Haystack Terraform State"
    echo "   -h     usage. Prints this message"
    echo
}



function verifyArgs() {

    if [[ -z $ACTION ]]; then
        ACTION=install-all
    fi

    if [[ -z $CLUSTER_TYPE ]]; then
        CLUSTER_TYPE=local
    fi

    if [[ -z $APP_VARS_FILE ]]; then
        APP_VARS_FILE=$DIR/cluster/$CLUSTER_TYPE/apps/overrides.json
        if [ ! -f ${APP_VARS_FILE} ]; then
            echo "{}" >> ${APP_VARS_FILE}
        fi
    fi

    if [[ -z $CLUSTER_NAME ]]; then
        CLUSTER_NAME=haystack
    fi

    if ! [[ ${CLUSTER_NAME} =~ ^[a-z]+[a-z0-9-]*$ ]]; then
        echo "Invalid cluster name format ${CLUSTER_NAME}, special characters and starting with numeric values are not allowed"
        display_help
        exit 1
    fi

    if [[ -z $INFRA_VARS_FILE ]]; then
        INFRA_VARS_FILE=$DIR/cluster/$CLUSTER_TYPE/infrastructure/overrides.json
        if [ ! -f ${INFRA_VARS_FILE} ]; then
            echo "{}" >> ${INFRA_VARS_FILE}
        fi
    fi

    if [[ ( -z $S3_BUCKET ) && "$CLUSTER_TYPE" = "aws" ]]; then
        echo "flag --s3-bucket|-sb needs to be passed when cluster_type is aws"
        exit 1
    fi

    if [[ -z ${SKIP_APPROVAL} ]]; then
        SKIP_APPROVAL="false"
    fi

    if [[ -z ${GET_HAYSTACK_STATE} ]]; then
        GET_HAYSTACK_STATE="false"
    fi

    if [ ! -f ${APP_VARS_FILE} -o ! -f ${INFRA_VARS_FILE} ]; then
        echo "Error: Overrides file provided is not a file or not readable"
        display_help
        exit 1
    fi

    echo
    echo "Starting ${0} with arguments"
    echo
    echo "action = ${ACTION}"
    echo "cluster-type = ${CLUSTER_TYPE}"
    echo "cluster-name = ${CLUSTER_NAME}"
    echo "appvars-file-path = ${APP_VARS_FILE}"
    echo "infravars-file-path = ${INFRA_VARS_FILE}"
    echo "skip-approval = ${SKIP_APPROVAL}"
    echo "get-haystack-state = ${GET_HAYSTACK_STATE}"
    echo
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

    if [ ! -f $TERRAFORM ] || [ ! -f $KUBECTL ] || [ ! -f $KOPS ]; then
        $DIR/install-third-party-softwares.sh
    fi
}

function getHaystackState() {
    if [[ "$GET_HAYSTACK_STATE" = "true" ]]; then
        echo "Fetching Haystack State"
        case "$CLUSTER_TYPE" in
            aws)
                cd $DIR/cluster/$CLUSTER_TYPE/infrastructure
                $TERRAFORM init -backend-config="bucket=$S3_BUCKET" -backend-config="key=terraform/$CLUSTER_NAME-infrastructure"
                echo "Fetching Haystack Infrastructure State"
                INFRASTRUCTURE_STATE=`$TERRAFORM state pull`

                cd $DIR/cluster/$CLUSTER_TYPE/apps
                $TERRAFORM init -backend-config="bucket=$S3_BUCKET" -backend-config="key=terraform/$CLUSTER_NAME-apps"
                echo "Fetching Haystack Apps State"
                APPS_STATE=`$TERRAFORM state pull`

                echo "Haystack State"
                HAYSTACK_STATE='{"infrastructureState":'"$INFRASTRUCTURE_STATE"',"appsState":'"$APPS_STATE"'}'
                echo "HAYSTACK_STATE_START"
                echo $HAYSTACK_STATE
                echo "HAYSTACK_STATE_END"
            ;;

            local)
                cd $DIR/cluster/$CLUSTER_TYPE/infrastructure
                $TERRAFORM init
                echo "Fetching Haystack Infrastructure State"
                INFRASTRUCTURE_STATE=`$TERRAFORM state pull`

                cd $DIR/cluster/$CLUSTER_TYPE/apps
                $TERRAFORM init
                echo "Fetching Haystack Apps State"
                APPS_STATE=`$TERRAFORM state pull`

                echo "Haystack State"
                HAYSTACK_STATE='{"infrastructureState":'"$INFRASTRUCTURE_STATE"',"appsState":'"$APPS_STATE"'}'
                echo "HAYSTACK_STATE_START"
                echo $HAYSTACK_STATE
                echo "HAYSTACK_STATE_END"
            ;;
        esac
    else
        echo "$GET_HAYSTACK_STATE"
    fi
}

function deleteState() {

    case "$CLUSTER_TYPE" in
        local)
            echo "deleting state folder..."
            rm -rf $DIR/cluster/$CLUSTER_TYPE/state

            # https://unix.stackexchange.com/questions/115863/delete-files-and-directories-by-their-names-no-such-file-or-directory
            echo "deleting .terraform folders..."
            find $DIR -name ".terraform" -prune -exec rm -r {} \;
        ;;

        ?)
            echo "state deletion is currently only supported for local deployment"
            display_help
            exit 1
        ;;
    esac
}

function command_exists() {
    type "$1" &> /dev/null;
}

function applyActionOnComponents() {
    echo "Applying action ${ACTION}"
    case "$ACTION" in
        install-all)
            installInfrastructure
            installComponents
            getHaystackState
            echo "Congratulations! you've successfully created haystack infrastructure and deployed haystack apps"
        ;;
        install-apps)
            installComponents
            getHaystackState
            echo "Congratulations! you've successfully redeployed haystack apps"
        ;;
        uninstall-apps)
            uninstallComponents
            echo "Congratulations! you've successfully uninstalled haystack apps"
        ;;
        uninstall-all)
            uninstallComponents
            uninstallInfrastructure
            echo "Congratulations! you've successfully and deleted haystack apps and destroyed haystack infrastructure"
        ;;
        delete-state)
            deleteState
            echo "Congratulations! you've successfully deleted the deployment state"
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
    if [ "$SKIP_APPROVAL" = "true" ]; then
        FORCE_FLAG="-force"
    else
        echo "$SKIP_APPROVAL"
    fi
    echo "Deleting haystack apps using terraform"
    case "$CLUSTER_TYPE" in
        aws)
            $TERRAFORM init -backend-config="bucket=$S3_BUCKET" -backend-config="key=terraform/$CLUSTER_NAME-apps"
            #setting the correct kubectl config for terraform
            DOMAIN_NAME=$(echo "var.domain_name" | $TERRAFORM console -var-file=$APP_VARS_FILE)
            echo "setting kubectl context : $CLUSTER_NAME-k8s.$DOMAIN_NAME"
            $KOPS export kubecfg --name $CLUSTER_NAME-k8s.$DOMAIN_NAME --state s3://$S3_BUCKET
            $TERRAFORM destroy $FORCE_FLAG -var-file=$APP_VARS_FILE -var haystack_cluster_name=$CLUSTER_NAME -var s3_bucket_name=$S3_BUCKET -var kubectl_executable_name=$KUBECTL
        ;;

        local)
            $TERRAFORM init
            $TERRAFORM destroy $FORCE_FLAG -var-file=$APP_VARS_FILE -var kubectl_executable_name=$KUBECTL
        ;;
    esac
}

function uninstallInfrastructure() {
    cd $DIR/cluster/$CLUSTER_TYPE/infrastructure
    if [ "$SKIP_APPROVAL" = "true" ]; then
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
            $TERRAFORM destroy $FORCE_FLAG -var-file=$INFRA_VARS_FILE -var "cluster={ name = \"$CLUSTER_NAME\", s3_bucket_name = \"$S3_BUCKET\" }"  -var kubectl_executable_name=$KUBECTL -var kops_executable_name=$KOPS
        ;;

        local)
            $TERRAFORM init
            $TERRAFORM destroy $FORCE_FLAG -var-file=$INFRA_VARS_FILE -var kubectl_executable_name=$KUBECTL -var docker_host_ip=$(minikube ip)
        ;;
    esac
}

function installInfrastructure() {

    cd $DIR/cluster/$CLUSTER_TYPE/infrastructure
    if [ "$SKIP_APPROVAL" = "true" ]; then
        AUTO_APPROVE="-auto-approve"
    else
        echo "$SKIP_APPROVAL"
    fi
    echo "Creating haystack infrastructure using terraform "
    case "$CLUSTER_TYPE" in
        aws)
            $TERRAFORM init -backend-config="bucket=$S3_BUCKET" -backend-config="key=terraform/$CLUSTER_NAME-infrastructure"
            #setting the correct kubectl config for terraform
            DOMAIN_NAME=$(echo "var.domain_name" | $TERRAFORM console -var-file=$INFRA_VARS_FILE)
            echo "setting kubectl context : $CLUSTER_NAME-k8s.$DOMAIN_NAME"
            $KOPS export kubecfg --name $CLUSTER_NAME-k8s.$DOMAIN_NAME --state s3://$S3_BUCKET || true
            $TERRAFORM apply $AUTO_APPROVE -var-file=$INFRA_VARS_FILE -var "cluster={ name = \"$CLUSTER_NAME\", s3_bucket_name = \"$S3_BUCKET\" }" -var kubectl_executable_name=$KUBECTL -var kops_executable_name=$KOPS
        ;;

        local)
            $TERRAFORM init
            $TERRAFORM apply $AUTO_APPROVE -var-file=$INFRA_VARS_FILE -var kubectl_executable_name=$KUBECTL -var docker_host_ip=$(minikube ip)
        ;;
    esac
}

function installComponents() {

    cd $DIR/cluster/$CLUSTER_TYPE/apps
    if [ "$SKIP_APPROVAL" = "true" ]; then
        AUTO_APPROVE="-input=false -auto-approve"
    else
        echo "$SKIP_APPROVAL"
    fi

    echo "deploying haystack-apps using terraform"

    case "$CLUSTER_TYPE" in
        aws)
            $TERRAFORM init -backend-config="bucket=$S3_BUCKET" -backend-config="key=terraform/$CLUSTER_NAME-apps"
            #setting the correct kubectl config for terraform
            DOMAIN_NAME=$(echo "var.domain_name" | $TERRAFORM console -var-file=$APP_VARS_FILE)
            echo "setting kubectl context : $CLUSTER_NAME-k8s.$DOMAIN_NAME"
            $KOPS export kubecfg --name $CLUSTER_NAME-k8s.$DOMAIN_NAME --state s3://$S3_BUCKET
            $TERRAFORM apply $AUTO_APPROVE -var-file=$APP_VARS_FILE -var haystack_cluster_name=$CLUSTER_NAME -var s3_bucket_name=$S3_BUCKET -var kubectl_executable_name=$KUBECTL
        ;;

        local)
            $TERRAFORM init
            $TERRAFORM apply $AUTO_APPROVE -var-file=$APP_VARS_FILE -var kubectl_executable_name=$KUBECTL
        ;;

        ?)
            display_help
            exit 1
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

########
# main
########
while getopts "hsc:r:n:b:i:o:t" OPTION
do
    case $OPTION in
        h)
            display_help
            exit 1
        ;;
        c)
            CLUSTER_TYPE=$OPTARG
        ;;
        r)
            ACTION=$OPTARG
        ;;
        n)
            CLUSTER_NAME=$OPTARG
        ;;
        b)
            S3_BUCKET=$OPTARG
        ;;
        i)
            INFRA_VARS_FILE=$OPTARG
        ;;
        o)
            APP_VARS_FILE=$OPTARG
        ;;
        s)
            SKIP_APPROVAL="true"
        ;;
        t)
            GET_HAYSTACK_STATE="true"
        ;;
        ?)
            display_help
            exit 1
        ;;
    esac
done

# sanitize the arguments passed to the script, and set the defaults correctly
verifyArgs

# verify minikube is running in local mode
verifyK8sCluster

# download third party softwares like kubectl, gomplate, jq etc.
downloadThirdPartySoftwares

# install/delete the haystack components
applyActionOnComponents
