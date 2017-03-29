 ARQUILLIAN RESIN 4 CONTAINER ADAPTER
 ====================

This is the Arquillian Plugin for the Resin 4 Java EE 6 server 
by Caucho Technologies (http://www.caucho.com/)

 Prerequisites
 ------------------------
 a) Java
 b) Apache Maven 3
 c) Git
 d) m2e (optional)
 

 Building the Plugin:
 --------------------

1. Create a workspace for Arquillian, e.g.
    /home/yourUsername/work/resin

2. Checkout Caucho's Arquillian Repository

    a) If you want to adjust the code and get your changes upstream, do the following:
        Fork & Clone Caucho's Arquillian Repo
            - Go to https://github.com/caucho/arquillian-container-resin
            - Click the fork button on the top right of the page
            - Clone your own fork by doing the following
                git clone git@github.com:/yourGithubUsername/arquillian-container-resin
    b) If you just want to build the module yourself
        Clone Caucho's Arquillian Repo
            - git clone git://github.com/caucho/arquillian-container-resin.git

3. Build the module
    You should be in the directory
        /home/yourUsername/work/resin/arquillian-container-resin
    Now type to build and install the module
        mvn clean install
