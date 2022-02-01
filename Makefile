
antlr   := java -jar $(PWD)/lib/antlr-4.7.1-complete.jar
visitor := -no-listener -visitor
java    = javac -g -Xmaxerrs 5 -Xlint:deprecation -classpath $(PWD)/src/:$(PWD)/build/:$(PWD)/lib/antlr-4.7.1-complete.jar
jarname := mecaPlanner-`cat VERSION`.jar

builtClasses := build/mecaPlanner/ $(wildcard build/mecaPlanner/*.class) $(wildcard build/mecaPlanner/*/*.class) $(wildcard build/mecaPlanner/*/*/*.class)

.PHONY: clean planner simulator demo example test efp2depl

planner: meca

meca: mecaPlanner.jar
	echo '#!/bin/bash' > ./meca
	echo 'java -cp "./mecaPlanner.jar:./lib/*" mecaPlanner.Planner "$$@"' >> ./meca
	chmod +x ./meca
	echo '#!/bin/bash' > ./mecad
	echo 'java -ea -cp "./mecaPlanner.jar:./lib/*" mecaPlanner.Planner "$$@"' >> ./mecad
	chmod +x ./mecad

mecaPlanner.jar: $(builtClasses)
	jar -cf $(jarname) -C build depl/ -C build mecaPlanner/ && ln -fs $(jarname) mecaPlanner.jar

$(builtClasses) : build/depl/ src/mecaPlanner/ $(wildcard src/mecaPlanner/*) $(wildcard src/mecaPlanner/*/*) $(wildcard src/mecaPlanner/*/*/*)
	$(java) src/mecaPlanner/*.java src/mecaPlanner/*/*.java -d build/

build/depl/: src/deplParser/DeplToProblem.java build/deplSrc/
	$(java) src/deplParser/DeplToProblem.java build/deplSrc/*.java -d build/

build/deplSrc/: src/deplParser/Depl.g4
	cd src/deplParser; $(antlr) $(visitor) -package depl Depl.g4 -o ../../build/deplSrc



simulator: mecaPlanner.jar src/tools/Simulator.java
	$(java) src/tools/Simulator.java -d build/
	echo '#!/bin/bash' > ./sim
	echo 'java -ea -cp "./mecaPlanner.jar:./lib/*:./build/" tools.Simulator $$@' >> ./sim
	chmod +x ./sim

demo: mecaPlanner.jar src/tools/Demo.java
	$(java) src/tools/Demo.java -d build/
	echo '#!/bin/bash' > ./demo
	echo 'java -ea -cp "./mecaPlanner.jar:./lib/*:./build/" tools.Demo $$@' >> ./demo
	chmod +x ./demo

test: mecaPlanner.jar src/tools/Test.java
	$(java) src/tools/Test.java -d build/
	echo '#!/bin/bash' > ./test
	echo 'java -ea -cp "./mecaPlanner.jar:./lib/*:./build/" tools.Test $$@' >> ./test
	chmod +x ./test

example: mecaPlanner.jar src/tools/Example.java
	$(java) src/tools/Example.java -d build/
	echo '#!/bin/bash' > ./example
	echo 'java -ea -cp "./mecaPlanner.jar:./lib/*:./build/" tools.Example $$@' >> ./example
	chmod +x ./example

actions: mecaPlanner.jar src/tools/Actions.java
	$(java) src/tools/Actions.java -d build/
	echo '#!/bin/bash' > ./actions
	echo 'java -ea -cp "./mecaPlanner.jar:./lib/*:./build/" tools.Actions $$@' >> ./actions
	chmod +x ./actions

parseFormula: mecaPlanner.jar src/tools/ParseFormula.java
	$(java) src/tools/ParseFormula.java -d build/
	echo '#!/bin/bash' > ./parseFormula
	echo 'java -ea -cp "./mecaPlanner.jar:./lib/*:./build/" tools.ParseFormula $$@' >> ./parseFormula
	chmod +x ./parseFormula


bisimulations: mecaPlanner.jar src/tools/Bisimulations.java
	$(java) src/tools/Bisimulations.java -d build/
	echo '#!/bin/bash' > ./bisimulations
	echo 'java -ea -cp "./mecaPlanner.jar:./lib/*:./build/" tools.Bisimulations $$@' >> ./bisimulations
	chmod +x ./bisimulations

kr2021: mecaPlanner.jar src/tools/KR2021.java
	$(java) src/tools/KR2021.java -d build/
	echo '#!/bin/bash' > ./kr2021
	echo 'java -ea -cp "./mecaPlanner.jar:./lib/*:./build/" tools.KR2021 $$@' >> ./kr2021
	chmod +x ./kr2021



efp2depl: build/efp2depl/
	echo '#!/bin/bash' > ./efp2depl
	echo 'java -cp ".:./build:./lib/*" efp2depl.EfpToDepl $$@' >> ./efp2depl
	chmod +x ./efp2depl
	echo '#!/bin/bash' > ./translateAllEfp
	echo 'for f in ~/planners/efp/EpistemicPlanning/Experiment/*.txt; do echo $f; d=`echo $(basename $f) | sed "s/\.txt/\.depl/"`;./efp2depl $f > problems/efp/$d;done' >> ./translateAllEfp
	chmod +x ./translateAllEfp

build/efp2depl/: src/translators/efp2depl/EfpToDepl.java build/efp2deplSrc/
	$(java) src/translators/efp2depl/*.java build/efp2deplSrc/*.java -d build/

build/efp2deplSrc/: src/translators/efp2depl/Efp.g4
	cd src/translators/efp2depl; $(antlr) $(visitor) -package efp2depl Efp.g4 -o ../../../build/efp2deplSrc



epddl2depl: build/epddl2depl/
	echo '#!/bin/bash' > ./epddl2depl
	echo 'java -cp ".:./build:./lib/*" epddl2depl.EpddlToDepl $$@' >> ./epddl2depl
	chmod +x ./epddl2depl
	echo '#!/bin/bash' > ./translateAllMepk
	echo 'for f in `find ~/planners/mepk/benchmarks/ -name *epddl -not -name *WordRooms*`;do echo $f; ./epddl2depl $f;done' >> ./translateAllMepk
	chmod +x ./translateAllMepk

build/epddl2depl/: src/translators/epddl2depl/EpddlToDepl.java build/epddl2deplSrc/
	$(java) src/translators/epddl2depl/*.java build/epddl2deplSrc/*.java -d build/

build/epddl2deplSrc/: src/translators/epddl2depl/Epddl.g4
	cd src/translators/epddl2depl; $(antlr) $(visitor) -package epddl2depl Epddl.g4 -o ../../../build/epddl2deplSrc



clean:
	rm -rf ./build
	rm -f ./meca
	rm -f ./mecad
	rm -f ./sim
	rm -f ./demo
	rm -f ./example
	rm -f ./actions
	rm -f ./kr2021
	rm -f ./efp2depl
	rm -f ./translateAllEfp
	rm -f ./epddl2depl
	rm -f ./translateAllMepk
	rm -f ./test
	rm -f ./bisimulations
	rm -f ./parseFormula
	rm -f ./mecaPlanner*.jar
