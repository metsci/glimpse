
mvn -f pom.xml versions:set -DnewVersion=%1
mvn -f pom.xml versions:update-child-modules -N

mvn clean compile jar:jar install

mvn -f assembly/assembly-full/pom.xml versions:update-parent -DparentVersion=[%1] -DallowSnapshots=true
mvn -f assembly/assembly-core/pom.xml versions:update-parent -DparentVersion=[%1] -DallowSnapshots=true

mvn -f assembly/assembly-full/pom.xml versions:commit
mvn -f assembly/assembly-core/pom.xml versions:commit
mvn -f pom.xml versions:commit
