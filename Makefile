

antlr   := java -jar $(PWD)/lib/antlr-4.7.1-complete.jar
visitor := -no-listener -visitor
java    = javac -g -Xmaxerrs 4 -Xlint:deprecation -classpath $(PWD)/src/:$(PWD)/build/:$(PWD)/lib/antlr-4.7.1-complete.jar
jarname := mecaPlanner-`cat VERSION`.jar

builtClasses := build/mecaPlanner/ $(wildcard build/mecaPlanner/*.class) $(wildcard build/mecaPlanner/*/*.class) $(wildcard build/mecaPlanner/*/*/*.class)

.PHONY: clean planner simulator demo example test efp2depl debug


planner: meca

debug: java += -g
debug: planner

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
	$(java) src/mecaPlanner/*.java src/mecaPlanner/*/*.java src/mecaPlanner/*/*/*.java -d build/

build/depl/: src/deplParser/DeplToProblem.java build/deplSrc/
	$(java) src/deplParser/DeplToProblem.java build/deplSrc/*.java -d build/

build/deplSrc/: src/deplParser/Depl.g4
	cd src/deplParser; $(antlr) $(visitor) -package depl Depl.g4 -o ../../build/deplSrc


simulator: mecaPlanner.jar
	$(java) src/tools/Simulator.java -d build/
	echo '#!/bin/bash' > ./sim
	echo 'java -ea -cp "./mecaPlanner.jar:./lib/*:./build/" tools.Simulator "$$@"' >> ./sim
	chmod +x ./sim

demo: mecaPlanner.jar
	$(java) src/tools/Demo.java -d build/
	echo '#!/bin/bash' > ./demo
	echo 'java -ea -cp "./mecaPlanner.jar:./lib/*:./build/" tools.Demo "$$@"' >> ./demo
	chmod +x ./demo

test: mecaPlanner.jar
	$(java) src/tools/Test.java -d build/
	echo '#!/bin/bash' > ./test
	echo 'java -ea -cp "./mecaPlanner.jar:./lib/*:./build/" tools.Test "$$@"' >> ./test
	chmod +x ./test

example: mecaPlanner.jar
	$(java) src/tools/Example.java -d build/
	echo '#!/bin/bash' > ./example
	echo 'java -ea -cp "./mecaPlanner.jar:./lib/*:./build/" tools.Example "$$@"' >> ./example
	chmod +x ./example
	echo '#!/bin/bash' > ./debug
	echo 'jdb -classpath "./mecaPlanner.jar:./lib/*:./build/" tools.Example "$$@"' >> ./debug
	chmod +x ./debug

actions: mecaPlanner.jar
	$(java) src/tools/Actions.java -d build/
	echo '#!/bin/bash' > ./action
	echo 'java -ea -cp "./mecaPlanner.jar:./lib/*:./build/" tools.Actions "$$@"' >> ./actions
	chmod +x ./actions

efp2depl: build/efp2depl/

build/efp2depl/: src/translators/efp2depl/EfpToDepl.java build/efp2deplSrc/
	$(java) src/translators/efp2depl/*.java build/efp2deplSrc/*.java -d build/

build/efp2deplSrc/: src/translators/efp2depl/Efp.g4
	cd src/translators/efp2depl; $(antlr) $(visitor) -package efp2depl Efp.g4 -o ../../../build/efp2deplSrc



epddl2depl: build/epddl2depl/

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
	rm -f ./debug
	rm -f ./example
	rm -f ./actions
	rm -f ./test
	rm -f ./mecaPlanner*.jar
