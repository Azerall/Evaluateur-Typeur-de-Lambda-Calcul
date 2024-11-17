# Variables
JAVAC = javac
MAIN = Main
SOURCES = Main.java Part2.java Part3.java Part4.java Part6.java Exceptions.java Parser.java

# Cibles
all: $(MAIN)

$(MAIN): $(SOURCES)
	$(JAVAC) $(SOURCES)

run: all
	java $(MAIN)

clean:
	rm -f *.class
