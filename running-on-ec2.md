## Setup
### AWS Account
1. set aws console account using your email
2. verify your email
3. provide your card details
4. set up a budget
5. always select ```us-east-1``` as the region.

### EC2 Launch
1. allocated elastic for use with ec2.
2. create a security group which:-
    1. has an outbound rule that allows traffic to go out from your server.
    2. has inbound rules that allows incoming :-
        1. HTTPS traffic on TCP port 443 from your ip or from everywhere.
        2. HTTP traffic on TCP port 80 from your ip or from everywhere.
        3. SSH on port 22 from your ip or from everywhere.
        4. custom port 8080 
2. create a rsa key pair for ec2 user. This will be used by putty for ssh.
    1. public key is stored on instance. private key stays on your machine.
    2. key pair can be generated in pem or ppk format. ppk works with putty.
    3. provide port 22 for ssh in putty.
    4. provide ppk key downloaded in putty(connection-->ssh-->auth)
3. select amazon linux 2, architecture 64-bit x86.
4. rest options will be default mostly, falling under aws free tier.
5. enable assign public ip option is set.
6. create iam role and attach it to your ec2 instance. 
   1. Go to IAM > Roles in AWS Console.
   2. Click Create role.
   3. Choose AWS service → EC2.
   4. Choose AWS managed policy --> AmazonSSMManagedInstanceCore
7. Navigate to end of Advanced Details on EC2 launch and provide [download-install.sh](shell-scripts/download-install.sh) as User data.