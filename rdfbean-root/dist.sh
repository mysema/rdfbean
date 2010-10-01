#!/bin/sh
rm -rf target/dist
mkdir target
mkdir target/dist

echo "Creating javadocs"
mvn javadoc:aggregate

echo "Creating reference documentation"
cd ../rdfbean-docs
mvn -Dxslthl.config=http://docbook.sourceforge.net/release/xsl/current/highlighting/xslthl-config.xml clean package
mkdir ../rdfbean-root/target/dist/reference
cp -R target/docbook/publish/en-US/* ../rdfbean-root/target/dist/reference/
cd ../rdfbean-root

echo "done."
