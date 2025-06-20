# Ensure your ec2 instance has been allocated an elastic ip before running below steps

# installing nginx
# Nginx will act as your reverse proxy and SSL/TLS terminator.
sudo dnf install -y nginx
sudo systemctl start nginx
sudo systemctl enable nginx

# Let's Encrypt provides free, automated, and open certificates. Certbot is a client that automates the process of obtaining and renewing these certificates.
# sudo dnf install epel-release -y
sudo dnf install certbot python3-certbot-nginx -y
sudo chown -R ec2-user:ec2-user /etc/nginx
sudo chmod 755 -R /etc/nginx
# interactive process to modify Nginx configuration, and set up automatic renewals.
# below will fail if the elastic ip given in square space, has not been allocated to your ec2.
sudo certbot --nginx --non-interactive --agree-tos --email rohit23ahuja@gmail.com --domain rohitahuja.dev --domain www.rohitahuja.dev

sudo vi /etc/nginx/conf.d/rohitahuja.dev.conf

# define domain configuration
# add below content in this file
# This server block handles HTTP requests and redirects them to HTTPS
# the next block is for https
server {
listen 80;
server_name rohitahuja.dev www.rohitahuja.dev;

    # This ensures Certbot can renew your certificate later via the HTTP challenge
    location /.well-known/acme-challenge/ {
        root /usr/share/nginx/html; # Standard Nginx web root for .well-known challenges
    }

    # Redirect all HTTP traffic to HTTPS
    return 301 https://$host$request_uri;
}

server {
listen 443 ssl;
server_name rohitahuja.dev www.rohitahuja.dev;

    # SSL Certificate Paths (Certbot has saved them here)
    ssl_certificate /etc/letsencrypt/live/rohitahuja.dev/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/rohitahuja.dev/privkey.pem;

    # Recommended SSL settings from Certbot (copied from default Nginx configuration usually)
    include /etc/letsencrypt/options-ssl-nginx.conf; # This file should have been created by certbot
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem; # This file should have been created by certbot

    # Proxy requests to your Spring Boot application
    location / {
        proxy_pass http://localhost:8085; # Or http://127.0.0.1:8085;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# test nginx config
sudo nginx -t

# restart nginx config
sudo systemctl restart nginx