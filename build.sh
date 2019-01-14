#!/bin/sh

git clone https://github.com/renespeck/knowledgeextraction
cd knowledgeextraction
mvn compile install --quiet
cd ..


git clone https://github.com/dice-group/Ocelot
cd Ocelot
mvn compile install -DskipTests=true --quiet 
cd ..


mvn compile