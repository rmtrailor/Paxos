# Proposal
For Project 3, my idea is to implement **Paxos** into my distributed system already created in Project 2.
I will, however, use the new Project 3 repository to keep the two projects distinct. An issue with my
Project 2 was that my implementation attempted a *pseudo*-consensus that ended up being weak and not
properly supported. With this project, I hope to build upon that by modifying my distributed backend
to support Paxos.

For this implementation, I aim to implement and rebuild my project with Paxos in three steps. **First**, 
I will need initial setup and reconstruction of the project. Essentially, my initial application needs
to be setup in a barebones state so that I can incrementally add the different parts of Paxos.
**Second**, I will implement the first part of the protocol phase which is the Prepare phase. This
includes both the preparation and promise aspects of the node communication. **Third**, I will
implement the second and third part of the protocol phase which is the Accept and Commit phase.
Of course, any available time afterwards will be spent testing and fixing any issues that come up.
 
***Edit (April 24, 2017):***

After our Project 3 discussion last class, the project proposal is now updated to Sami's suggested idea,
which involves creating a basic client that interacts with Paxos and sends values to the Paxos nodes
for a given value to be agreed upon.

My plan now involves creating a basic client that will send values to a Paxos Layer. This is a
minimal framework that will create the initial Paxos nodes and facilitate any requests from the client.
Essentially, the client only sees and interacts with the Paxos Layer, and the Paxos Layer will create the
the actual request and send it to the requested Paxos Node. My plan to build this project will
still follow the initial steps (first, second, third), as noted in the original proposal.