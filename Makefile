JFLAGS = -g
JC = javac
JVM= java

.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java
CLASSES = sender.java receiver.java
SENDER = sender
RECEIVER = receiver
SENDER_IP = 10.249.210.1
SENDER_REC_PORT = 4358
RECEIVER_IP = 10.208.20.9
RECEIVER_PORT = 4357

default: all
all: $(CLASSES:.java=.class)

receiver: $(RECEIVER).class
	$(JVM) $(RECEIVER) $(RECEIVER_PORT) $(SENDER_REC_PORT)
sender: $(SENDER).class
	$(JVM) $(SENDER) $(RECEIVER_IP) $(RECEIVER_PORT)

clean:
	rm -f *.class
