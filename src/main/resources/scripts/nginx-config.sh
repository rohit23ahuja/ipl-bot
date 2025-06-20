sudo yum update -y
sudo yum install nginx -y

sudo systemctl start nginx
sudo systemctl enable nginx

sudo nano /etc/nginx/nginx.conf
# (or check /etc/nginx/conf.d/default.conf if it exists)

#Modify the server block. You'll want to adjust the existing server block (or create a new one within http block if
#you're editing nginx.conf directly) to look something like this:
server {
    listen 80;
    listen [::]:80;
    server_name rohitahuja.dev www.rohitahuja.dev; # Replace with your domain

    location / {
        proxy_pass http://localhost:8085; # Your Spring Boot app is on port 80
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Add other configurations like static file serving, error pages, etc. if needed
}

sudo nginx -t
sudo systemctl reload nginx

#Certbot is the easiest way to get Let's Encrypt certificates.
# First, enable EPEL (Extra Packages for Enterprise Linux) repository
sudo yum install epel-release -y
sudo yum update -y

# Then install Certbot
sudo yum install certbot python3-certbot-nginx -y
# Or if python3-certbot-nginx isn't available, just certbot:
# sudo yum install certbot -y

# Now, use Certbot to get the certificate. Certbot has an Nginx plugin that can automate the configuration of Nginx for HTTPS.
sudo certbot --nginx -d rohitahuja.dev -d www.rohitahuja.dev

# Certbot will communicate with Let's Encrypt, verify your domain ownership (by creating temporary files on your web server
# that Let's Encrypt can access via HTTP), download the certificate, and configure your Nginx server block to use HTTPS and handle redirects.

# test the renewal process
sudo certbot renew --dry-run

# Create the Nginx configuration file for your application
# This configures Nginx to listen on port 80 for the initial Certbot challenge
# and then act as a reverse proxy for your application.
configure_nginx() {
  sudo chown -R ec2-user:ec2-user /etc/nginx
  sudo chmod 755 -R /etc/nginx
  cat <<EOF > /etc/nginx/conf.d/${YOUR_DOMAIN}.conf
  server {
      listen 80;
      server_name ${YOUR_DOMAIN};

      # This location is used by Certbot for domain validation
      location /.well-known/acme-challenge/ {
          root /usr/share/nginx/html;
      }

      # After validation, Certbot will update this block to redirect all HTTP to HTTPS
      location / {
          return 301 https://\$host\$request_uri;
      }
  }
EOF
# Enable and start Nginx service temporarily for Certbot
  sudo systemctl enable nginx
  sudo systemctl start nginx
# Make sure your app is running on localhost:${APP_PORT} before proceeding.
# Request SSL Certificate from Let's Encrypt using Certbot
# The --nginx flag automatically modifies the Nginx config for SSL.
# The --non-interactive flag makes it run without manual prompts.
  sudo certbot --nginx --non-interactive --agree-tos --email ${YOUR_EMAIL} --domain ${YOUR_DOMAIN}

# Certbot automatically modifies the Nginx config to add the SSL server block.
# Now, we will add the proxy_pass configuration to the new HTTPS server block.
# This is a robust way to inject the proxy configuration into the SSL server block created by Certbot.
  sudo awk -v app_port="$APP_PORT" '
/listen 443 ssl;/ {
    print;
    print "    location / {";
    print "        proxy_pass http://127.0.0.1:" app_port ";";
    print "        proxy_set_header Host \$host;";
    print "        proxy_set_header X-Real-IP \$remote_addr;";
    print "        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;";
    print "        proxy_set_header X-Forwarded-Proto \$scheme;";
    print "    }";
    next;
}
/location \/ \{/ {
    in_location_block = 1;
}
/}/ {
    if (in_location_block) {
        in_location_block = 0;
        next;
    }
}
!in_location_block {
    print;
}
' /etc/nginx/conf.d/${YOUR_DOMAIN}.conf > /etc/nginx/conf.d/${YOUR_DOMAIN}.conf.tmp && mv /etc/nginx/conf.d/${YOUR_DOMAIN}.conf.tmp /etc/nginx/conf.d/${YOUR_DOMAIN}.conf

# Test Nginx configuration and restart the service to apply all changes
  sudo nginx -t
  sudo systemctl restart nginx

# Enable the Certbot renewal timer to run automatically
  sudo systemctl enable certbot-renew.timer
  sudo systemctl start certbot-renew.timer

}


