########################################
# Variables

# JUnit 4 JAR location.  Allow local files to override system ones.
junitJar := $(wildcard $(junit) junit4.jar ~/opt/junit4.jar /usr/share/java/junit4.jar)
ifndef junitJar
$(error Cannot find the JUnit 4 JAR.  Add some alternative locations to the makefile or assign variable 'junit' on the command line)
else
junitJar := $(firstword $(junitJar))
endif

# Project layout
javaSrcDir := src
javaTstDir := src
javaPkgDir := com/github/afbarnard/jcsv

# Java class path
classpath := $(CURDIR)/$(javaSrcDir):$(junitJar)

# Java sources
javaSrcFiles := $(shell find $(javaSrcDir) -name '*.java' -not -name '*Test.java' | sort)
javaTstFiles := $(shell find $(javaTstDir) -name '*Test.java' | sort)

# Java classes
javaSrcClasses := $(javaSrcFiles:.java=.class)
javaTstClasses := $(javaTstFiles:.java=.class)

# List all the phony targets (targets that are really commands, not files)
.PHONY: listconfig tests clean allclean

########################################
# Non-Java / General / Meta Targets


# Variables for string substitution
emptyString :=
space := $(emptyString) $(emptyString)
indent := $(emptyString)    $(emptyString)

# List variables and values
listconfig:
	@echo Variables:
	@echo classpath: $(classpath)
	@echo javaSrcFiles:
	@echo -e "$(indent)$(subst $(space),\n$(indent),$(javaSrcFiles))"
	@echo javaSrcClasses:
	@echo -e "$(indent)$(subst $(space),\n$(indent),$(javaSrcClasses))"
	@echo javaTstFiles:
	@echo -e "$(indent)$(subst $(space),\n$(indent),$(javaTstFiles))"
	@echo javaTstClasses:
	@echo -e "$(indent)$(subst $(space),\n$(indent),$(javaTstClasses))"


########################################
# Java

# General Java compilation
%.class: %.java
	javac -cp $(classpath) -source 5 -Xlint $<

# Dependencies
$(javaSrcDir)/$(javaPkgDir)/ArrayQueue.class:
$(javaSrcDir)/$(javaPkgDir)/Dialect.class:
$(javaSrcDir)/$(javaPkgDir)/Lexer.class: $(addprefix $(javaSrcDir)/$(javaPkgDir)/,Dialect.class StreamBufferChar.class Token.class)
$(javaSrcDir)/$(javaPkgDir)/StreamBuffer.class:
$(javaSrcDir)/$(javaPkgDir)/Token.class:

# Tests' dependencies.  These have to be listed explicitly (not a
# pattern rule) for make to recognize and use them.
$(javaSrcDir)/$(javaPkgDir)/ArrayQueueTest.class: $(javaSrcDir)/$(javaPkgDir)/ArrayQueue.class
$(javaSrcDir)/$(javaPkgDir)/LexerTest.class: $(addprefix $(javaSrcDir)/$(javaPkgDir)/,Lexer.class TestText.class)
$(javaSrcDir)/$(javaPkgDir)/StreamBufferTest.class: $(javaSrcDir)/$(javaPkgDir)/StreamBuffer.class
$(javaSrcDir)/$(javaPkgDir)/TestText.class:

#####
# JUnit

# Run unit tests
tests: $(javaTstClasses)
	java -cp $(classpath) org.junit.runner.JUnitCore $(subst /,.,$(subst $(javaSrcDir)/,,$(javaTstClasses:.class=)))

#####
# Primitive versions of generic classes

# Name, constructors: 'StreamBuffer(<E>)?' -> 'StreamBufferChar'
# Type: '?E?' -> '?char?'
# Array: 'Object[' -> 'char['
# Remove unnecessary suppression of unchecked cast.
# Remove redundant cast.
$(javaSrcDir)/$(javaPkgDir)/StreamBufferChar.java: $(javaSrcDir)/$(javaPkgDir)/StreamBuffer.java
	sed -e 's/StreamBuffer\(<E>\)\?/StreamBufferChar/' -e 's/\(\W\)E\(\W\)/\1char\2/' -e 's/Object\[/char[/g' -e '/@SuppressWarnings("unchecked")/ d' -e 's/(char)//' $< > $@


########################################
# Cleanup

# Remove all derived files
clean:
	@find -name '*.class' -delete

# Named allclean to distinguish from clean* when typing
allclean: clean
	@rm -f $(javaSrcDir)/$(javaPkgDir)/StreamBufferChar.java
	@find -name '*~' -delete
