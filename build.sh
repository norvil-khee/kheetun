#!/bin/bash
#
mkdir kheetun 2> /dev/null
rm -rf kheetun/*
mkdir kheetun/lib
mkdir kheetun/etc
mkdir kheetun/log
mkdir kheetun/bin

cd kheetun-client
mvn package
mvn install
cd ..

cd kheetun-server 
mvn package
cd ..

cp -r kheetun-*/target/lib/* kheetun/lib
cp -r kheetun-*/target/*jar kheetun/bin

cp kheetun-server/src/main/resources/kheetund.rc kheetun/bin/kheetund

mv kheetun/bin/client*.jar kheetun/bin/kheetun-client.jar
mv kheetun/bin/server*.jar kheetun/bin/kheetun-server.jar

cd kheetun

cat << EOF > kheetun.list
%product kheetun
%copyright (c) Norvil Khee (norvil@norvil-khee.de)
%vendor http://www.norvil-khee.de
%license ./COPYING
%readme ./README
%version 0.1
%packager Norvil Khee 
%description Your friendly tunnel manager

%postinstall << _EOF
service kheetund restart
_EOF

%preremove << _EOF
service kheetund stop
_EOF

%system linux
\$prefixInitd=/etc
\$prefixRcd=/etc

# add init script
#
%system !aix !hpux
f 555 root sys \$prefixInitd/init.d/kheetund ./bin/kheetund
l 0755 root sys \$prefixRcd/rc3.d/S99kheetund \$prefixInitd/init.d/kheetund
l 0755 root sys \$prefixRcd/rc0.d/K00kheetund \$prefixInitd/init.d/kheetund
l 0755 root sys \$prefixRcd/rc1.d/K00kheetund \$prefixInitd/init.d/kheetund
l 0755 root sys \$prefixRcd/rc3.d/K00kheetund \$prefixInitd/init.d/kheetund

EOF


find . -type d | sed 's/^\.//' | while read d ; do
    echo d 755 root sys /opt/kheetun$d >> kheetun.list
done

find . -type f | sed 's/^\..//' | grep -v kheetun.list | while read f ; do
    echo f 644 root sys /opt/kheetun/$f ./$f >> kheetun.list
done


cp ../kheetun-client/src/main/resources/kheetun.sh bin/kheetun
echo "f 755 root root /opt/kheetun/bin/kheetun ./bin/kheetun" >> kheetun.list

epm -f deb kheetun

cp linux*/*.deb ..

cd ..


