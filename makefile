########################################
# Variables

# JUnit 4 JAR location.  Allow local files to override system ones.
junitJar := $(wildcard $(junit) junit4.jar ~/opt/junit4.jar /usr/share/java/junit4.jar)
ifndef junitJar
$(error Cannot find the JUnit 4 JAR.  Add some alternative locations to the makefile or assign variable 'junit' on the command line)
else
junitJar := $(firstword $(junitJar))
endif

# Compiler options (e.g. -source 5)
javacOpts := -source 8 -target 8 -Xlint -bootclasspath /usr/lib/jvm/jre-1.8.0/lib/rt.jar

# Project layout
javaSrcDir := src
javaTstDir := src
javaPkgDir := com/github/afbarnard/jcsv
javaBldDir := bld

# Java class path
classpath := $(CURDIR)/$(javaBldDir):$(junitJar)

# Java sources
javaSrcFiles := $(shell find $(javaSrcDir) -name '*.java' -not -name '*Test.java' | sort)
javaTstFiles := $(shell find $(javaTstDir) -name '*Test.java' | sort)

# Java classes
javaSrcClasses := $(subst $(javaSrcDir),$(javaBldDir),$(javaSrcFiles:.java=.class))
javaTstClasses := $(subst $(javaSrcDir),$(javaBldDir),$(javaTstFiles:.java=.class))

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

# Build directory
$(javaBldDir)/.exists:
	mkdir -p $(@D)
	@touch $@


########################################
# Java

# General Java compilation
$(javaBldDir)/%.class: $(javaBldDir)/.exists $(javaSrcDir)/%.java
	javac $(javacOpts) -cp $(classpath) -d $(javaBldDir) $(word 2,$^)

# Dependencies
$(javaBldDir)/$(javaPkgDir)/ArrayQueue.class:
$(javaBldDir)/$(javaPkgDir)/Dialect.class:
$(javaBldDir)/$(javaPkgDir)/Lexer.class: $(addprefix $(javaBldDir)/$(javaPkgDir)/,ArrayQueue.class Dialect.class StreamBufferChar.class StreamBuffer.class Token.class)
$(javaBldDir)/$(javaPkgDir)/StreamBuffer.class:
$(javaBldDir)/$(javaPkgDir)/StreamBufferChar.class: $(javaSrcDir)/$(javaPkgDir)/StreamBufferChar.java
$(javaBldDir)/$(javaPkgDir)/Token.class:

# Tests' dependencies.  These have to be listed explicitly (not a
# pattern rule) for make to recognize and use them.
$(javaBldDir)/$(javaPkgDir)/ArrayQueueTest.class: $(javaBldDir)/$(javaPkgDir)/ArrayQueue.class
$(javaBldDir)/$(javaPkgDir)/LexerTest.class: $(addprefix $(javaBldDir)/$(javaPkgDir)/,Lexer.class TestText.class)
$(javaBldDir)/$(javaPkgDir)/StreamBufferTest.class: $(javaBldDir)/$(javaPkgDir)/StreamBuffer.class
$(javaBldDir)/$(javaPkgDir)/TestText.class:

#####
# JUnit

# Run unit tests
tests: $(javaTstClasses)
	java -cp $(classpath) org.junit.runner.JUnitCore $(subst /,.,$(subst $(javaBldDir)/,,$(javaTstClasses:.class=)))

#####
# Primitive versions of generic classes

# Remove parts that only make sense for reference types
# Name, constructors: 'StreamBuffer(<E>)?' -> 'StreamBufferChar'
# Type: '?E?' -> '?char?'
# Array: 'Object[' -> 'char['
# Remove unnecessary suppression of unchecked cast.
# Remove redundant cast.
# 'Supplier<char>' -> 'Supplier<Char>'
$(javaSrcDir)/$(javaPkgDir)/StreamBufferChar.java: $(javaSrcDir)/$(javaPkgDir)/StreamBuffer.java
	sed -e '/start reference types only/,/end reference types only/ d' -e 's/StreamBuffer\(<E>\)\?/StreamBufferChar/' -e 's/\(\W\)E\(\W\)/\1char\2/' -e 's/Object\[/char[/g' -e '/@SuppressWarnings("unchecked")/ d' -e 's/(char)//' -e 's/Supplier<char>/Supplier<Character>/' $< > $@


########################################
# Cleanup

# Remove all derived files
clean:
	@find -name '*.class' -delete

# Named allclean to distinguish from clean* when typing
allclean: clean
	@-rm -R $(javaBldDir) $(javaSrcDir)/$(javaPkgDir)/StreamBufferChar.java
	@find -name '*~' -delete
