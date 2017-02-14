#!/bin/bash
#
# dirty dirty little package script
#
sudo mkdir _build 2> /dev/null
sudo rm -rf _build/*
sudo chown ${USER}: _build
mkdir _build/lib
mkdir _build/etc
mkdir _build/log
mkdir _build/bin

version=`grep public.*VERSION src/main/java/org/khee/kheetun/Kheetun.java | perl -p -e 's/^.*(\d+.\d+.\d+(\-rc\d+)?).*$/$1/g'`

echo "Building kheetun v$version" 

mvn clean
mvn package

cp -r target/lib/* _build/lib

cp src/main/package/kheetund.rc _build/etc/kheetund.rc
cp src/main/package/kheetund.default _build/etc/kheetund.default
cp src/main/package/kheetun.desktop _build/etc/kheetun.desktop
cp src/main/package/kheetun16x16.png _build/etc/kheetun16x16.png
cp src/main/package/kheetun32x32.png _build/etc/kheetun32x32.png
cp src/main/package/kheetun64x64.png _build/etc/kheetun64x64.png
cp LICENSE _build/etc
cp CHANGELOG.md _build/etc

mv target/kheetun-*.jar _build/bin/kheetun.jar

cd _build

cat << EOF > kheetun.list
%product kheetun, pretty friendly ssh tunnel manager
%copyright (c) Sir Norvil Khee (volker@superhein.de)
%vendor http://www.khee.org
%license ./LICENSE
%readme ./README
%version $version
%packager Sir Norvil Khee 
%requires libjava-gnome-jni
%description 2 component ssh tunnel manager. SSH tunnels and hosts file managed by daemon running as privileged user. Config managed by user client.

%postinstall << _EOF
[ ! -f ${HOME}/.kheetun/log4j2.xml ] && echo "Generating log4j2.xml for client in ${HOME}/.kheetun/log4j2.xml" && touch ${HOME}/.kheetun/log4j2.xml && chown ${USER}: ${HOME}/.kheetun/log4j2.xml && cat <<'_LOG4J' > ${HOME}/.kheetun/log4j2.xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration>
  <Appenders>
    <RollingFile name="FileLogger" fileName="\$\${sys:user.home}/.kheetun/kheetun.log" filePattern="\$\${sys:user.home}/.kheetun/kheetun.log.%i.gz">
      <PatternLayout pattern="%d %t %-5p - %m%n"/>
      <Policies>
          <SizeBasedTriggeringPolicy size="10 MB"/>
      </Policies>
      <DefaultRolloverStrategy max="10"/>
    </RollingFile>
    <Console name="Console" target="SYSTEM_OUT">
      <PatternLayout pattern="%d %t %-5p - %m%n"/>
    </Console>
  </Appenders>
  
  <Loggers>
    <Root level="info">
      <AppenderRef ref="FileLogger"/>
      <AppenderRef ref="Console"/>
    </Root>
  </Loggers>
</Configuration>
_LOG4J

[ ! -f /opt/kheetun/etc/log4j2.xml ]  && echo "Generating log4j2.xml for server in /opt/kheetun/etc/log4j2.xml" && cat <<_LOG4J > /opt/kheetun/etc/log4j2.xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration>
  <Appenders>
    <RollingFile name="FileLogger" fileName="/opt/kheetun/log/kheetund.log" filePattern="/opt/kheetun/log/kheetund.log.%i.gz">
      <PatternLayout pattern="%d %t %-5p - %m%n"/>
      <Policies>
          <SizeBasedTriggeringPolicy size="10 MB"/>
      </Policies>
      <DefaultRolloverStrategy max="10"/>
    </RollingFile>
    <Console name="Console" target="SYSTEM_OUT">
      <PatternLayout pattern="%d %t %-5p - %m%n"/>
    </Console>
  </Appenders>
  
  <Loggers>
    <Root level="info">
      <AppenderRef ref="FileLogger"/>
      <AppenderRef ref="Console"/>
    </Root>
  </Loggers>
</Configuration>
_LOG4J

update-rc.d kheetund defaults
service kheetund restart
_EOF

%preremove << _EOF
service kheetund stop
update-rc.d -f kheetund remove
_EOF

%system linux
\$prefixInitd=/etc
\$prefixRcd=/etc

# add init script
#
%system !aix !hpux
f 555 root sys \$prefixInitd/init.d/kheetund ./etc/kheetund.rc
f 644 root sys \$prefixInitd/default/kheetund ./etc/kheetund.default

# common files
#
f 644 root sys /usr/share/icons/hicolor/16x16/apps/kheetun.png ./etc/kheetun16x16.png
f 644 root sys /usr/share/icons/hicolor/32x32/apps/kheetun.png ./etc/kheetun32x32.png
f 644 root sys /usr/share/icons/hicolor/64x64/apps/kheetun.png ./etc/kheetun64x64.png
f 644 root sys /usr/share/applications/kheetun.desktop ./etc/kheetun.desktop

EOF


find . -type d | sed 's/^\.//' | while read d ; do
    echo d 755 root sys /opt/kheetun$d >> kheetun.list
done

find . -type f | sed 's/^\..//' | grep -v kheetun.list | grep -v kheetund.rc | grep -v kheetund.default | grep -v kheetun.png | grep -v kheetun.desktop | while read f ; do
    echo f 644 root sys /opt/kheetun/$f ./$f >> kheetun.list
done


cp ../src/main/package/kheetun.sh bin/kheetun
echo "f 755 root root /opt/kheetun/bin/kheetun ./bin/kheetun" >> kheetun.list

sudo epm -f deb -nsm kheetun

sudo cp linux*/*.deb ..

cd ..


