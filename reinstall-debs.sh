#/bin/sh
cd "$(dirname "$0")"

p1=`find ckan-deploy-debian-cli/target | grep '\.deb$'`

sudo dpkg -i "$p1"

