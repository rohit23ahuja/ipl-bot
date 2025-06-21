## Setup
### AWS Account
1. set aws console account using your email
2. verify your email
3. provide your card details
4. set up a budget
5. select ```eu-west-2``` as the region. may be faster in external network

### Security group setup 
1. Create a security group which :-
    1. has an outbound rule that allows traffic to go out from your server. 
    2. has inbound rules that allows incoming :-
        1. HTTPS traffic on TCP port 443 from your ip or from everywhere.
        2. HTTP traffic on TCP port 80 from your ip or from everywhere.
        3. SSH on port 22 from your ip or from everywhere.
2. Create db secruity group which :-
    1. has an inbound rule that allows incoming TCP traffic from everywhere for port 5432. 

### RDS Launch
1. Go RDS service, select Postgres engine version.
2. Select Free tier template for your db
3. Provide master password for your db.
4. Attach the created security group to this DB instance.
5. De select enable performance insights to save on unnecessary costs.
6. Allow public access
7. use pgadmin to test connection to this DB instance from your local machine.
8. de select enable automated back ups
9. de select enhanced monitoring
10. de select enable minor version upgrade
11. de select enable encryption
12. Create database ```ai_learn```

### IAM Role set up
1. create iam role and attach it to your ec2 instance.
    1. Go to IAM > Roles in AWS Console.
    2. Click Create role.
    3. Choose AWS service → EC2.
    4. Choose AWS managed policy --> AmazonSSMManagedInstanceCore
    5. to get parameters from aws system manager --> parameter store.

### AWS System manager parameter store
1. in region eu-west-2
2. laie-springai/openai-api-key
3. laie-springai/documents-path
4. laie-springai/weather-api-key
5. laie-springai/email-username
6. laie-springai/email-password
7. laie-springai/db-host
8. laie-springai/db-password
9. laie-springai/openai-chat-model

### EC2 Launch
1. allocated elastic for use with ec2.
2. attach security group created above. 
3. create a rsa key pair for ec2 user. This will be used by putty for ssh.
4. connect using ec2 instance connect in external network, to avoid putty
5. select amazon linux 2, architecture 64-bit x86.
6. rest options will be default mostly, falling under aws free tier.
7. enable assign public ip option is set.
8. Navigate to end of Advanced Details on EC2 launch and provide [download-install.sh](scripts/download-install.sh) as User data.

