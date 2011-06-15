
SPATO_PATH = ../../SPaTo/SPaTo_Visual_Explorer

P5LIB_PATH = $(SPATO_PATH)/support/Processing.app/Contents/Resources/Java
P5LIBS = $(P5LIB_PATH)/core.jar

LIBNAME = tGUI

$(LIBNAME).jar: *.java resources/*
	rm -rf $(LIBNAME)
	javac -target 1.5 -cp $(P5LIBS) -Xlint -d . *.java
	cp -r resources $(LIBNAME)/
	jar -cf $(LIBNAME).jar $(LIBNAME)
	cp $(LIBNAME).jar $(SPATO_PATH)/code/
