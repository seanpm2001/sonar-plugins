sonardir=/opt/sonar-2.0.1
arch=macosx-universal-64
mvn clean install
rm $sonardir/extensions/plugins/sonar-sonarj-plugin*
cp target/sonar-sonarj-plugin*.jar $sonardir/extensions/plugins
$sonardir/bin/$arch/sonar.sh start

