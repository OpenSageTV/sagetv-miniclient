#!/bin/bash

CLEAR_ERROR="\e[49m\e[0m\e[0m"
ERROR="\e[41m\e[97m\e[1m"
BOLD="\e[1m"
CLEAR_BOLD="\e[0m"
echo '--------------------------------------------------------------------------------'
echo '|  ___          _    _                      ___  _   _  _      ______ __   __  |'
echo '| / _ \        | |  | |                    |_  || | | || |    |___  //  | /  | |'
echo '|/ /_\ \ _   _ | |_ | |__    ___   _ __      | || | | || |       / / `| | `| | |'
echo '||  _  || | | || __|| `_ \  / _ \ | `__|     | || | | || |      / /   | |  | | |'
echo '|| | | || |_| || |_ | | | || (_) || |    /\__/ /\ \_/ /| |____./ /   _| |__| |_|'
echo '|\_| |_/ \__,_| \__||_| |_| \___/ |_|    \____/  \___/ \_____/\_/    \___/\___/|'
echo '|                                                                              |'
echo '|                                                                              |'
echo -e "| ${BOLD}Amazon App Store Publishing Script${CLEAR_BOLD}                                           |"
echo '| Date: 2/14/2022                                                              |'
echo '| Depends: curl, jq, less                                                            |'
echo '| Ref: https://developer.amazon.com/docs/app-submission-api/appsub-api-ref.html|'
echo '| https://github.com/jvl711                                                    |'
echo '|                                                                              |'
echo '--------------------------------------------------------------------------------'
echo ''


API_VERSION="v1"
SHOW_USAGE=false


#----------------------------------------------------- Verify Input -------------------------------------------------------

if [ -z "$1" ]; then

	SHOW_USAGE=true

else

	APP_ID="$1"

fi

if [ -z "$2" ]; then

	SHOW_USAGE=true

else

	if [ -f "$2" ]; then

    	APK_FILENAME="@$2"

	else

		echo -e "${ERROR}Apk file does not exist with the name and path given${CLEAR_ERROR}" 1>&2
		echo ''
		SHOW_USAGE=true

	fi

fi

if [ -z "$3" ]; then

	SHOW_USAGE=true

else

	CLIENT_ID="$3"

fi

if [ -z "$4" ]; then

	SHOW_USAGE=true

else

	CLIENT_SECRET="$4"

fi

if [ -z "$5" ]; then

	SHOW_USAGE=true

else

	CHANGELIST_PATH="$5"

fi

if [ "$SHOW_USAGE" = true ]; then

	echo -e "${BOLD}Usage: $0 APK_ID APK_FILENAME CLIENT_ID CLIENT_SECRET${CLEAR_BOLD}"
	echo ''
	echo -e "APK_ID: The indentifier for the amazon application your are publishing to"
	echo -e "APK_FILENAME: Filename and path to the Apk file that your are uploading"
	echo -e "CLIENT_ID: The client_id needed to generate the authentication token"
	echo -e "CLIENT_SECRET: The client_secret needed to generate the authentication token"
	echo -e "CHANGELIST_PATH: Text file containing a list of changes for this release"
	echo ''

	exit 1

else

  WORKINGDIR=`pwd`

  echo "Current working dir: $WORKINGDIR"
	echo -e "${BOLD}Using to update listing:${CLEAR_BOLD}"
	echo "APP_ID=$APP_ID"
	echo "APK_FILENAME=$APK_FILENAME"
	echo "CHANGELIST_PATH=$CHANGELIST_PATH"

fi

#------------------------------------------------------- Get TOKEN --------------------------------------------------------

echo "Getting token..."

curl -sS -k -X POST -d "grant_type=client_credentials&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET&scope=appstore::apps:readwrite" \
	-H 'Content-Type: application/x-www-form-urlencoded' https://api.amazon.com/auth/O2/token > token

if [ $? -eq 0 ] && [ -f token ]
then

	TOKEN=`jq -r .access_token token`
	echo "Getting token successful: $TOKEN"

else
	echo "Error getting token from amazon " $? >&2
	exit 1
fi

#------------------------------------------------------- Get Active Edit  --------------------------------------------------------

echo "Checking for active listing edit..."

curl -sS -k -X GET "https://developer.amazon.com/api/appstore/$API_VERSION/applications/$APP_ID/edits" -H "Authorization: Bearer $TOKEN"  > edit

edit=$(<edit)

if [ $? -ne 0 ]
then

	echo "Error getting active edit" $? >&2
	exit 1

elif [ $edit == '{}' ]
then

	EDIT_ID=''
	echo "No active edit was found"

else

	EDIT_ID=`jq -r .id edit`
	echo "Getting active listing edit successful: $EDIT_ID"

fi

#------------------------------------------------------- Create Edit If Needed --------------------------------------------------------

if [ -z $EDIT_ID ]
then

	echo "Creating a listing edit..."

	curl -sS -k -X POST "https://developer.amazon.com/api/appstore/$API_VERSION/applications/$APP_ID/edits" -H "Authorization: Bearer $TOKEN"  > edit

	edit=$(<edit)

	if [ $? -ne 0 ]
	then

		echo "Error creating listing edit" $? >&2
		exit 1

	elif [ $edit == '{}' ]
	then

		EDIT_ID=''
		echo "Error creating listing edit"

	else

		EDIT_ID=`jq -r .id edit`
		echo "Creating listing edit successful: $EDIT_ID"

	fi

fi

#------------------------------------------------------- Get Listing Details --------------------------------------------------------

echo "Getting listing details..."

curl -D headers -sS -k -X GET "https://developer.amazon.com/api/appstore/$API_VERSION/applications/$APP_ID/edits/$EDIT_ID/listings/en-US" \
	-H "Authorization: Bearer $TOKEN"  > listing

if [ $? -ne 0 ] && [ ! -f $listing ]
then

	echo "Error getting listing details"

else

	echo "Getting listing details successful"

	LISTING_ETAG=`less headers | grep ETag | awk -F ' ' '{print $2}'`

	echo "Listing ETag: $LISTING_ETAG"
fi

#------------------------------------------------------- Update Listing Details --------------------------------------------------------

if [ ! -f $CHANGELIST_PATH ]
then

	echo "Error changelist file does not exist"
	ls ./pipeline
  exit 1

else

  data=`cat $CHANGELIST_PATH`

fi

jq --arg update "$data" '.recentChanges=$update' listing > listing_updated

echo "Updating listing details..."

curl -sS -k -X PUT "https://developer.amazon.com/api/appstore/$API_VERSION/applications/$APP_ID/edits/$EDIT_ID/listings/en-US" \
	-H "Authorization: Bearer $TOKEN" -H "If-Match: $LISTING_ETAG" -H "Content-Type: application/json" -d @listing_updated > /dev/null

if [ $? -ne 0 ]
then

	echo "Error updating listing details"
	exit 1

else

	echo "Updating listing details successful"

fi

#------------------------------------------------------- Get APKs --------------------------------------------------------

echo "Getting listing APKs..."

curl -sS -k -X GET "https://developer.amazon.com/api/appstore/$API_VERSION/applications/$APP_ID/edits/$EDIT_ID/apks" \
	-H "Authorization: Bearer $TOKEN"  > apks

apks=$(<apks)

if [ $? -ne 0 ]
then

	echo "Error error getting APK list details" $? >&2
	exit 1

elif [ $apks == '{}' ]
then

	APK_ID=''
	echo "Error getting APK id from list"
	exit 1

else

	APK_ID=`jq -r .[0].id apks`
	echo "First APKs ID: $APK_ID"

fi

#------------------------------------------------------- Get APK Details --------------------------------------------------------

echo "Getting APK details..."

curl -D headers -sS -k -X GET "https://developer.amazon.com/api/appstore/$API_VERSION/applications/$APP_ID/edits/$EDIT_ID/apks/$APK_ID" \
	-H "Authorization: Bearer $TOKEN"  > apk

apk=$(<apk)

if [ $? -ne 0 ]
then

	echo "Error error getting apk details" $? >&2
	exit 1

elif [ $apks == '{}' ]
then

	APK_ID=''
	echo "Error getting apk details"
	exit 1

else

	echo "Getting APK details successful"

	APK_ETAG=`less headers | grep ETag | awk -F ' ' '{print $2}'`
	echo "APK ETag: " $APK_ETAG

fi

#------------------------------------------------------- Upload new APK --------------------------------------------------------

echo "Uploading APK..."

curl -sS -k -X PUT "https://developer.amazon.com/api/appstore/$API_VERSION/applications/$APP_ID/edits/$EDIT_ID/apks/$APK_ID/replace" \
	-H "Authorization: Bearer $TOKEN" -H 'Expect:' -H "If-Match: $APK_ETAG" -H "Content-Type: application/octet-stream" --data-binary $APK_FILENAME > /dev/null

if [ $? -ne 0 ]
then

	echo "Error uploading APK" $? >&2
	exit 1

else

	echo "APK uploaded successfully"


fi

#------------------------------------------------------- Clean up --------------------------------------------------------

rm edit
rm token
rm headers
rm listing
rm listing_updated
rm apk
rm apks

