set -ex

mvn clean install -P karaf

./redeploy.sh

mvn -pl integration -pl api-request -P test test

reef-karaf-*-dist/bin/stop

