#!/bin/bash

# убрать /.. 1 штуку в ср
# javadoc тоже касается
javac ./../info/kgeorgiy/ja/merkulov/implementor/Implementor.java -cp ./../../../modules/info.kgeorgiy.java.advanced.implementor -d .
jar cfm Impler.jar MANIFEST.MF info
rm -rf info
