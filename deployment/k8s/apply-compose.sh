#!/bin/bash
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"


#########################
# The command line help #
#########################
function display_help() {
    echo "Usage: $0 [option...] " >&2
    echo
    echo "   -a, --action               defines the action for deploying haystack components. possible values: install|uninstall, default: install"
    echo "   -u, --unit-name            applies the action on a deployable unit by its name, possible values: all|<component-name>, default: all"
    echo "   -e, --environment          choose the environment settings for cluster(aws) and deployable-units. possible values: dev|test|int|prod, default: dev"
    echo "   --use-context              kubectl context for installing haystack components, possible values: <valid-cluster-context> default: minikube-haystack-<env>"
    echo
    # echo some stuff here for the -a or --add-options 
    exit 1
}

while :
do
    case "$1" in
      -e | --environment)
          if [ $# -ne 0 ]; then
            ENVIRONMENT="$2"
          fi
          shift 2
          ;;
      -a | --action)
          if [ $# -ne 0 ]; then
            ACTION="$2"
          fi
          shift 2
          ;;
       -u | --unit-name)
          if [ $# -ne 0 ]; then
            UNIT_NAME="$2"
          fi
          shift 2
          ;;
       --use-context)
          if [ $# -ne 0 ]; then
            USE_CONTEXT="$2"
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
   ACTION=install
 fi

 if [[ -z $UNIT_NAME ]]; then
   UNIT_NAME=all
 fi

 if [[ -z $ENVIRONMENT ]]; then
   ENVIRONMENT=dev
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

function setGlobalVariables() {
    COMPOSE_JSON_FILE=/tmp/haystack-compose.json

    # convert the yaml file to json for easy querying using JQ tool
    sed 's/replace_with_minikube_ip/'$(minikube ip 2>/dev/null)'/g' compose/$ENVIRONMENT.yaml | \
    ruby -ryaml -rjson -e \
         'puts JSON.pretty_generate(YAML.load(ARGF))' > $COMPOSE_JSON_FILE

    rm -rf /tmp/haystack-compose.yaml
    # temporary location where we extract the configuration for every single deployable unit
    # this json is fed to gomplate to render the deployment.yaml for Kubernetes
    SINGLE_DEPLOYABLE_UNIT_CONFIG_JSON=/tmp/single-deployable-unit-config.json
    HAYSTACK_NAMESPACE=haystack-$ENVIRONMENT
}

function downloadThirdPartySoftwares() {
 setThirdPartySoftwareBasePath
 
 # variable for accessing third party softwares
 JQ=$THIRD_PARTY_SOFTWARE_PATH/jq
 GOMPLATE=$THIRD_PARTY_SOFTWARE_PATH/gomplate
 KUBECTL=$THIRD_PARTY_SOFTWARE_PATH/kubectl/1.6.7/kubectl
    
 if [ ! -f $KUBECTL ] || [ ! -f $GOMPLATE ] || [ ! -f $JQ ]; then
   $DIR/install-third-party-softwares.sh
 fi
}

function command_exists () {
    type "$1" &> /dev/null ;
}

function installClusterAddons() {
  case "$ACTION" in
    install)
        # iterate over all the addons
        local ADDONS=`cat $COMPOSE_JSON_FILE | $JQ '.cluster.addons'`
        local ADDONS_COUNT=`cat $COMPOSE_JSON_FILE | $JQ '.cluster.addons | length'`
        local i="0"
        while [ $i -lt $ADDONS_COUNT ]
         do
         local ADDON_JSON=`echo $ADDONS | $JQ '.['$i']'`
         local ADDON_PATH=`echo $ADDON_JSON | $JQ -r '.path'`
         if [[ -f $ADDON_PATH ]]; then
            echo "configuring addon '$ADDON_PATH' on the k8s cluster"
            local HAS_ARGS=`echo $ADDON_JSON | $JQ '. | has("params")'`
            if [[ $HAS_ARGS == true ]]; then
                `echo $ADDON_JSON | $JQ '.params' > $SINGLE_DEPLOYABLE_UNIT_CONFIG_JSON`
                cat $ADDON_PATH | $GOMPLATE -d config=$SINGLE_DEPLOYABLE_UNIT_CONFIG_JSON | $KUBECTL apply --record -f -
            else
              $KUBECTL apply -f $ADDON_PATH --record
            fi
         fi
         i=$[$i+1]
        done
        ;;
  *)
    echo "not deleting haystack components installed in kube-system namespace, we recommend to do that manually!"
    ;;
  esac
}

function verifyK8sCluster() {
  if [[ -z $USE_CONTEXT ]]; then
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
    setKubectlContext
  else
    setKubectlContext
    echo "Running 'kubectl get nodes' to verify the connectivity to the k8s cluster...."
    if ! $KUBECTL get nodes; then
        echo "Error! Fail to get the details of the k8s nodes."
        exit 1
    else
        echo "succesfully verified the connectivity to cluster."
    fi
  fi
}

function installComponent() {
  local COMPONENT_CONFIG_JSON=$(echo $*)

  # create the configmap
  createConfigMap $COMPONENT_CONFIG_JSON

  if [[ -n $CONFIG_MAP_NAME ]]; then
    echo $COMPONENT_CONFIG_JSON | $JQ '. + {"appConfigMapName": "'$CONFIG_MAP_NAME'"}' > $SINGLE_DEPLOYABLE_UNIT_CONFIG_JSON
  else
    echo $COMPONENT_CONFIG_JSON > $SINGLE_DEPLOYABLE_UNIT_CONFIG_JSON
  fi

  # render the deployment template using the config
  # run kubectl to create deployement and service
  cat templates/deployment.yaml | $GOMPLATE -d config=$SINGLE_DEPLOYABLE_UNIT_CONFIG_JSON | $KUBECTL apply --record -f -
}

function createProxyService() {
 local SERVICE_CONFIG_JSON=$(echo $*)
 echo $SERVICE_CONFIG_JSON > $SINGLE_DEPLOYABLE_UNIT_CONFIG_JSON

 # run kubectl to create service
 cat templates/proxy-service.yaml | $GOMPLATE -d config=$SINGLE_DEPLOYABLE_UNIT_CONFIG_JSON | $KUBECTL apply --record -f -
}

function createConfigMap() {
   local COMPONENT_CONFIG_JSON=$(echo $*)
   local COMPONENT_NAME=`echo $COMPONENT_CONFIG_JSON | $JQ -r '.name'`
   local APP_CONFIG_PATH=`echo $COMPONENT_CONFIG_JSON | $JQ -r '.volumes.appConfig.path'`

   if [[ -f $APP_CONFIG_PATH ]]; then
    local APP_CONFIG_CKSUM=0
    APP_CONFIG_CKSUM=`cat $APP_CONFIG_PATH | cksum | cut -f 1 -d ' '`

    # build configMap name using component's name and two cksums. Mark this global to be used during the deployment
    CONFIG_MAP_NAME=$COMPONENT_NAME-$APP_CONFIG_CKSUM

    # check if the configmap exists for this component and configs checksums
    if [[ `$KUBECTL get configmap $CONFIG_MAP_NAME 2>/dev/null` ]]; then
      echo "no change in config map for $COMPONENT_NAME"
    else
       CONFIG_FILE_NAME=$(basename $APP_CONFIG_PATH)
       $KUBECTL create configmap $CONFIG_MAP_NAME --from-file=$CONFIG_FILE_NAME=$APP_CONFIG_PATH --namespace=$HAYSTACK_NAMESPACE --generator='configmap/v1'
       # write the labels on configmap
       $KUBECTL label configmap $CONFIG_MAP_NAME app-name=$COMPONENT_NAME
    fi
   else
     unset CONFIG_MAP_NAME
   fi
}

function installScheduledJobs() {
  local SCHEDULED_JOBS=`cat $COMPOSE_JSON_FILE | $JQ .scheduled_jobs`
  local SCHEDULED_JOBS_COUNT=`echo $SCHEDULED_JOBS | $JQ '. | length'`

  local i="0"
  while [ $i -lt $SCHEDULED_JOBS_COUNT ]
  do
    # add the namespace attribute in the config before rendering the templates
    echo $SCHEDULED_JOBS | $JQ '.['$i'] + {"namespace":"'$HAYSTACK_NAMESPACE'"}' > $SINGLE_DEPLOYABLE_UNIT_CONFIG_JSON
    cat templates/scheduleJob.yaml | $GOMPLATE -d config=$SINGLE_DEPLOYABLE_UNIT_CONFIG_JSON | $KUBECTL apply --record -f -
   i=$[$i+1]
  done
}

function uninstallScheduledJobs() {
  case "$UNIT_NAME" in
       all)
          local SCHEDULED_JOBS=`cat $COMPOSE_JSON_FILE | $JQ .scheduled_jobs`
          local SCHEDULED_JOBS_COUNT=`echo $SCHEDULED_JOBS | $JQ '. | length'`

          local i="0"
          while [ $i -lt $SCHEDULED_JOBS_COUNT ]
          do
            local JOB_NAME=`echo $SCHEDULED_JOBS | $JQ -r '.['$i'].name'`
            $KUBECTL delete cronjob/$JOB_NAME 2>/dev/null
          i=$[$i+1]
          done
          ;;
    esac
}

function applyActionOnComponents() {
    case "$ACTION" in
       install)
          installComponents
          # creates service abstraction for external backend systems like databases,
          # it creates an alias within the k8s cluster to access the external resource so that its consistent accross environments(test/int/prod).
          installProxyServices

          #install scheduled jobs
          installScheduledJobs
          echo "Congratulations! all haystack components have been installed on k8s cluster"
          ;;
       uninstall)
          uninstallComponents
          uninstallProxyServices
          uninstallScheduledJobs
          echo "Congratulations! all haystack components have been uninstalled from k8s cluster"
          ;;
       *)
          echo "Error!!! Fail to understand the action type, see the help."
          display_help
          exit 1
          ;;
    esac
}

function deleteConfigMap() {
  local COMPONENT_NAME=$(echo $*)
  local CONFIG_MAP_JSON=`$KUBECTL get configmap --selector=app-name=$COMPONENT_NAME -o json`
  local CONFIG_MAP_LENGTH=`echo $CONFIG_MAP_JSON | $JQ '.items | length'`

  local i="0"
  while [ $i -lt $CONFIG_MAP_LENGTH ]
   do
    CONFIG_MAP_NAME=`echo $CONFIG_MAP_JSON | $JQ -r '.items['$i'].metadata.name'`
    $KUBECTL delete configmap $CONFIG_MAP_NAME
    i=$[$i+1]
  done
}

function uninstallComponent() {
   local COMPONENT_NAME=$(echo $*)
   # delete deployment
   if $KUBECTL get deploy/$COMPONENT_NAME 1>/dev/null 2>/dev/null; then
      $KUBECTL delete deploy/$COMPONENT_NAME
   else
     echo "No deployment found with unit name as: "$COMPONENT_NAME
   fi

   # delete service
   if $KUBECTL get service/$COMPONENT_NAME 1>/dev/null 2>/dev/null; then
      $KUBECTL delete service/$COMPONENT_NAME
   fi

   # delete hpa
   if $KUBECTL get hpa/$COMPONENT_NAME 1>/dev/null 2>/dev/null; then
      $KUBECTL delete hpa/$COMPONENT_NAME
   fi

   # delete configmap
   deleteConfigMap $COMPONENT_NAME
}

function uninstallProxyServices() {
    case "$UNIT_NAME" in
       all)
          local PROXY_SERVICES_JSON=`cat $COMPOSE_JSON_FILE | $JQ .proxy_services`
          # count of all the proxy services
          local PROXY_SERVICES_COUNT=`echo $PROXY_SERVICES_JSON | $JQ '. | length'`

          local i="0"
          while [ $i -lt $PROXY_SERVICES_COUNT ]
          do
            # add the namespace attribute in the config before rendering the templates
            local PROXY_NAME=`echo $PROXY_SERVICES_JSON | $JQ -r '.['$i'].name'`
            uninstallComponent $PROXY_NAME
          i=$[$i+1]
          done
          ;;
    esac
}

function uninstallComponents() {
    local DEPLOYABLE_UNITS_JSON=`cat $COMPOSE_JSON_FILE | $JQ .deployable_units`

    case "$UNIT_NAME" in
       all)
          # count of all the deployable units
          local DEPLOYABLE_UNITS_COUNT=`cat $COMPOSE_JSON_FILE | $JQ '.deployable_units | length'`

          local i="0"
          while [ $i -lt $DEPLOYABLE_UNITS_COUNT ]
          do
            # get the deployable unit name
            local COMPONENT_NAME=`echo $DEPLOYABLE_UNITS_JSON | $JQ -r '.['$i'].name'`
            uninstallComponent $COMPONENT_NAME
          i=$[$i+1]
          done
          ;;
       *)
          local COMPONENT_NAME=$(echo $DEPLOYABLE_UNITS_JSON | $JQ -r '.[] | select(.name=="'$UNIT_NAME'") | .name')
          if [[ $COMPONENT_NAME == '' ]]; then
            echo "Error!!! Fail to find the configuration for the component $UNIT_NAME in the compose file."
            exit 1
          else
            uninstallComponent $COMPONENT_NAME
          fi
          ;;
    esac
}

function installComponents() {
    local DEPLOYABLE_UNITS_JSON=`cat $COMPOSE_JSON_FILE | $JQ .deployable_units`

    case "$UNIT_NAME" in
       all)
          echo "All haystack components will be deployed in the namespace '$HAYSTACK_NAMESPACE'"

          # count of all the deployable units
          local DEPLOYABLE_UNITS_COUNT=`cat $COMPOSE_JSON_FILE | $JQ '.deployable_units | length'`

          local i="0"
          while [ $i -lt $DEPLOYABLE_UNITS_COUNT ]
          do
            # add the namespace attribute in the config before rendering the templates
            local CONFIG_JSON=`echo $DEPLOYABLE_UNITS_JSON | $JQ '.['$i'] + {"namespace":"'$HAYSTACK_NAMESPACE'"}'`
            installComponent $CONFIG_JSON
          i=$[$i+1]
          done
          ;;
       *)
          local CONFIG=$(echo $DEPLOYABLE_UNITS_JSON | $JQ '.[] | select(.name=="'$UNIT_NAME'")')
          if [[ $CONFIG == '' ]]; then
            echo "Error!!! Fail to find the configuration for the component '$UNIT_NAME' in the compose file"
            exit 1
          else
            local CONFIG_JSON=`echo $CONFIG | $JQ '. + {"namespace":"'$HAYSTACK_NAMESPACE'"}'`
            installComponent $CONFIG_JSON
          fi
          ;;
    esac
}

function installSecrets() {
  local SECRETS_JSON=`cat $COMPOSE_JSON_FILE | $JQ .secrets`
  # count all the secrets to be installed
  local SECRETS_COUNT=`echo $SECRETS_JSON | $JQ '. | length'`
  local i="0"
  while [ $i -lt $SECRETS_COUNT ]
  do
    # add the namespace attribute in the config before rendering the templates
    local SECRET_CONFIG_PATH=`echo $SECRETS_JSON | $JQ -r '.['$i'].path'`
    echo $SECRETS_JSON | $JQ '.['$i'].params + {"namespace":"'$HAYSTACK_NAMESPACE'"}' > $SINGLE_DEPLOYABLE_UNIT_CONFIG_JSON
    # run kubectl to install secret
    cat $SECRET_CONFIG_PATH | $GOMPLATE -d config=$SINGLE_DEPLOYABLE_UNIT_CONFIG_JSON | $KUBECTL apply --record -f -
    i=$[$i+1]
  done
}

function installProxyServices() {
    local PROXY_SERVICES_JSON=`cat $COMPOSE_JSON_FILE | $JQ .proxy_services`

    case "$UNIT_NAME" in
       all)
          echo "Deploying proxy services"
          # count of all the proxy services
          local SERVICE_UNITS_COUNT=`cat $COMPOSE_JSON_FILE | $JQ '.proxy_services | length'`
          local i="0"
          while [ $i -lt $SERVICE_UNITS_COUNT ]
          do
            # add the namespace attribute in the config before rendering the templates
            local CONFIG_JSON=`echo $PROXY_SERVICES_JSON | $JQ '.['$i'] + {"namespace":"'$HAYSTACK_NAMESPACE'"}'`
            createProxyService $CONFIG_JSON
          i=$[$i+1]
          done
          ;;
    esac
}

function setKubectlContext() {
  if [[ -z $USE_CONTEXT ]]; then
    echo "Creating the context 'minikube-haystack-$ENVIRONMENT' to make '$HAYSTACK_NAMESPACE' as implicit namespace for kubectl commands"
    $KUBECTL config set-context minikube-haystack-$ENVIRONMENT --namespace=$HAYSTACK_NAMESPACE --cluster=minikube --user=minikube
    $KUBECTL config use-context minikube-haystack-$ENVIRONMENT
  else
    echo "setting the kubectl context with $USE_CONTEXT"
    $KUBECTL config use-context $USE_CONTEXT
  fi
}

function createNamespaceIfNotExists() {
    # check if the namespace already exists
    if ! $KUBECTL get namespace/$HAYSTACK_NAMESPACE 1>/dev/null 2>/dev/null; then
       echo "Creating namespace with name '$HAYSTACK_NAMESPACE'"
       $KUBECTL create namespace --generator='namespace/v1' $HAYSTACK_NAMESPACE
    fi
}

function cleanupTempFiles() {
    rm -rf $COMPOSE_JSON_FILE $SINGLE_DEPLOYABLE_UNIT_CONFIG_JSON
}

# clean up all temp files created in previous run, if any
cleanupTempFiles

# sanitize the arguments passed to the script, and set the defaults correctly
verifyArgs

# download third party softwares like kubectl, gomplate, jq etc.
downloadThirdPartySoftwares

# set global variables.
setGlobalVariables

# verify the kubernetes cluster
verifyK8sCluster

# setup the namespace for this environment, naming convention is haystack-{{environment-name}}
createNamespaceIfNotExists

# store the secrets like certs etc in k8s. we dont delete the secrets as part of un-installation.
installSecrets

# deploy cluster addons like heapster, grafana etc.
installClusterAddons

# install/delete the haystack components
applyActionOnComponents
