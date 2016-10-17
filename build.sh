#!/bin/bash
#
# dirty dirty little package script
#
mkdir kheetun 2> /dev/null
sudo rm -rf kheetun/*
mkdir kheetun/lib
mkdir kheetun/etc
mkdir kheetun/log
mkdir kheetun/bin

version=`grep public.*VERSION kheetun-client/src/main/java/org/khee/kheetun/client/kheetun.java | perl -p -e 's/^.*(\d+.\d+.\d+).*$/$1/g'`

echo "Building kheetun v$version" 

cd kheetun-client
mvn clean
mvn package
mvn install
cd ..

cd kheetun-server 
mvn clean
mvn package
cd ..

cp -r kheetun-*/target/lib/* kheetun/lib
cp -r kheetun-*/target/*jar kheetun/bin

cp kheetun-server/src/main/resources/kheetund.rc kheetun/etc/kheetund.rc
cp kheetun-server/src/main/resources/kheetund.default kheetun/etc/kheetund.default
cp kheetun-client/src/main/resources/kheetun.desktop kheetun/etc/kheetun.desktop
cp kheetun-client/src/main/resources/kheetun.png kheetun/etc/kheetun.png
cp kheetun-server/LICENSE kheetun/etc
cp kheetun-client/src/main/resources/CHANGELOG.md kheetun/etc/CHANGELOG.md

mv kheetun/bin/client*.jar kheetun/bin/kheetun-client.jar
mv kheetun/bin/server*.jar kheetun/bin/kheetun-server.jar

cd kheetun

cat << EOF > kheetun.list
%product kheetun, pretty friendly ssh tunnel manager
%copyright (c) Norvil Khee (norvil@norvil-khee.de)
%vendor http://www.norvil-khee.de
%license ./LICENSE
%readme ./README
%version $version
%packager Norvil Khee 
%requires libjava-gnome-jni
%description 2 component ssh tunnel manager. SSH tunnels and hosts file managed by daemon running as privileged user. Config managed by user client.

%postinstall << _EOF
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
f 644 root sys /usr/share/icons/hicolor/32x32/apps/kheetun.png ./etc/kheetun.png
f 644 root sys /usr/share/applications/kheetun.desktop ./etc/kheetun.desktop

EOF


find . -type d | sed 's/^\.//' | while read d ; do
    echo d 755 root sys /opt/kheetun$d >> kheetun.list
done

find . -type f | sed 's/^\..//' | grep -v kheetun.list | grep -v kheetund.rc | grep -v kheetund.default | grep -v kheetun.png | grep -v kheetun.desktop | while read f ; do
    echo f 644 root sys /opt/kheetun/$f ./$f >> kheetun.list
done


cp ../kheetun-client/src/main/resources/kheetun.sh bin/kheetun
echo "f 755 root root /opt/kheetun/bin/kheetun ./bin/kheetun" >> kheetun.list

sudo epm -f deb -nsm kheetun

sudo cp linux*/*.deb ..

cd ..


