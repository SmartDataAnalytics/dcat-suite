#/bin/sh
cd "$(dirname "$0")"

p1=`find dcat-suite-debian-cli/target | grep '\.deb$'`

sudo dpkg -i "$p1"

