#!/bin/bash
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
UNIT_NAMES_ARRAY_INDEX="0"
declare -a UNIT_NAMES

#########################
# The command line help #
#########################
function display_help() {
    echo "Usage: $0 [option...] " >&2
    echo
    echo "   -a, --action               defines the action for deploying haystack components. possible values: install|uninstall, default: install"
    echo "   -u, --unit-name            applies the action on a deployable unit by its name, possible values: all|<component-name>, default: all (use separate -u for each unit)"
    echo "                              for example '-u zk -u kafka-service -u haystack-pipes-json-transformer' to start only the latter and the services on which it depends"
    echo "   -c, --cloud-provider       choose the cloud-provider settings for cluster. possible values: aws default: aws"
    echo "   -t, --terraform-parameters parameters which need to be passed to terraform eg : secret-key, access-key"

    echo
    # echo some stuff here for the -a or --add-options 
    exit 1
}

while :
do
    case "$1" in
      -e | --cloud-provider)
          if [ $# -ne 0 ]; then
            CLOUD_PROVIDER="$2"
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
            UNIT_NAMES[UNIT_NAMES_ARRAY_INDEX]="$2"
            UNIT_NAMES_ARRAY_INDEX=$(( UNIT_NAMES_ARRAY_INDEX + 1 ))
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
 if [[ -z $UNIT_NAMES ]]; then
   UNIT_NAMES[0]=all
 fi

 if [[ -z $CLOUD_PROVIDER ]]; then
   CLOUD_PROVIDER=aws
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
    
 if [ ! -f $TERRAFORM ]; then
   $DIR/install-third-party-softwares.sh
 fi
}

function command_exists () {
    type "$1" &> /dev/null ;
}


function applyActionOnComponents() {
    case "$ACTION" in
       install)
          installComponents
          echo "Congratulations! you've successfully created haystack infrastructure"
          ;;
       uninstall)
          uninstallComponents
          echo "Congratulations! you've successfully destroyed haystack infrastructure"
          ;;
       *)
          echo "Error!!! Fail to understand the action type, see the help."
          display_help
          exit 1
          ;;
    esac
}

function uninstallComponents() {
    echo "Deleting haystack infrastructure using terraform"
    $TERRAFORM init -backend-config=providers/aws/backend.tfvars providers/aws
    $TERRAFORM destroy -var-file=providers/aws/variables.tfvars  providers/aws
}

function installComponents() {

    echo "Creating haystack infrastructure using terraform"
    $TERRAFORM init -backend-config=providers/aws/backend.tfvars providers/aws
    $TERRAFORM apply -var-file=providers/aws/variables.tfvars  providers/aws
}



# sanitize the arguments passed to the script, and set the defaults correctly
verifyArgs

# download third party softwares like kubectl, gomplate, jq etc.
downloadThirdPartySoftwares

# install/delete the haystack components
applyActionOnComponents
