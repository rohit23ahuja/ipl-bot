#!/bin/bash

# Exit immediately if a command exits with a non-zero status.
set -e

echo "Starting Nginx and Certbot setup script..."
echo "------------------------------------------"

# --- Prerequisites Checklist ---
echo "Checking prerequisites (manual verification):"
echo "  1. Ensure your EC2 instance has been allocated an Elastic IP."
echo "  2. Ensure your AWS Security Group for this EC2 instance allows inbound traffic on:"
echo "     - TCP Port 80 (HTTP) from 0.0.0.0/0 (for Certbot verification and HTTP redirect)"
echo "     - TCP Port 443 (HTTPS) from 0.0.0.0/0 (for secure web traffic)"
echo "  3. Ensure your Squarespace DNS A records for 'rohitahuja.dev' and 'www.rohitahuja.dev' point to your Elastic IP."
echo "  4. Ensure your Spring Boot web application is running on port 8085 on this EC2 instance."
echo ""
echo "Press Enter to continue, or Ctrl+C to abort if prerequisites are not met."
read -r # Wait for user input

# --- 1. Installing Nginx ---
echo "Step 1: Installing Nginx..."
sudo dnf install -y nginx
sudo systemctl start nginx
sudo systemctl enable nginx
echo "Nginx installed, started, and enabled."

# --- 2. Installing Certbot ---
echo "Step 2: Installing Certbot and its Nginx plugin..."
# Note: For Amazon Linux 2023, epel-release might not be strictly necessary if certbot is in default repos,
# but it doesn't hurt to have it for broader package availability.
# sudo dnf install epel-release -y
sudo dnf install certbot python3-certbot-nginx -y
echo "Certbot and its Nginx plugin installed."

# --- 3. Adjusting Nginx Permissions (if necessary) ---
echo "Step 3: Adjusting Nginx configuration directory permissions for Certbot (if needed)..."
# These chmod/chown commands might be overly broad and generally not recommended
# unless specifically troubleshooting. Certbot usually handles necessary permissions.
# I am including them as per your provided content.
sudo chown -R ec2-user:ec2-user /etc/nginx || true # Using || true to prevent script from exiting if this fails
sudo chmod 755 -R /etc/nginx || true             # Using || true to prevent script from exiting if this fails
echo "Nginx permissions adjusted."

# --- NEW Step: 3.5. Create a basic Nginx server block for Certbot to find ---
# This minimal server block allows Certbot to automatically configure SSL.
# It will be overwritten by your detailed config later.
echo "Step 3.5: Creating a temporary Nginx server block for Certbot auto-configuration..."
NGINX_TEMP_CONF_FILE="/etc/nginx/conf.d/rohitahuja.dev.temp.conf" # Use a temp name
sudo tee "$NGINX_TEMP_CONF_FILE" > /dev/null << 'EOF'
server {
    listen 80;
    server_name rohitahuja.dev www.rohitahuja.dev app.rohitahuja.dev;
    # Certbot will add its challenge here, and then the SSL config.
    # This block will be replaced/modified by the comprehensive config later.
}
EOF
echo "Temporary Nginx config for Certbot created."
# Restart Nginx to load the temporary config, so Certbot can see it.
echo "Restarting Nginx to load temporary configuration..."
sudo systemctl restart nginx

# --- 4. Obtaining SSL Certificate with Certbot ---
echo "Step 4: Obtaining SSL certificate using Certbot..."
echo "This is a non-interactive process. Ensure your DNS is propagated and ports 80/443 are open."
sudo certbot --nginx --non-interactive --agree-tos --email rohit23ahuja@gmail.com --domain rohitahuja.dev --domain www.rohitahuja.dev --domain app.rohitahuja.dev

if [ $? -eq 0 ]; then
    echo "SSL certificate successfully obtained and saved."
    # After successful installation, remove the temporary config file
    sudo rm -f "$NGINX_TEMP_CONF_FILE"
    echo "Temporary Nginx config removed."
else
    echo "ERROR: Certbot failed to obtain or install the certificate. Please review the output above."
    echo "Check your domain's DNS, Elastic IP allocation, and Security Group rules (especially port 80)."
    exit 1
fi

# --- 5. Defining Domain Configuration for Nginx ---
echo "Step 5: Creating Nginx domain configuration file '/etc/nginx/conf.d/rohitahuja.dev.conf'..."
NGINX_CONF_FILE="/etc/nginx/conf.d/rohitahuja.dev.conf"

sudo tee "$NGINX_CONF_FILE" > /dev/null << 'EOF'
# This server block handles HTTP requests and redirects them to HTTPS
server {
    listen 80;
    server_name rohitahuja.dev www.rohitahuja.dev app.rohitahuja.dev;

    # This ensures Certbot can renew your certificate later via the HTTP challenge
    location /.well-known/acme-challenge/ {
        root /usr/share/nginx/html; # Standard Nginx web root for .well-known challenges
    }

    # Redirect all HTTP traffic to HTTPS
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl;
    server_name rohitahuja.dev www.rohitahuja.dev app.rohitahuja.dev;

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
EOF
echo "Nginx domain configuration written to $NGINX_CONF_FILE"

# --- 6. Test Nginx Configuration ---
echo "Step 6: Testing Nginx configuration syntax..."
sudo nginx -t

if [ $? -eq 0 ]; then
    echo "Nginx configuration syntax is OK."
else
    echo "ERROR: Nginx configuration syntax test failed. Please review the output above."
    exit 1
fi

# --- 7. Restart Nginx Service ---
echo "Step 7: Restarting Nginx service to apply changes..."
sudo systemctl restart nginx
echo "Nginx service restarted."

echo "------------------------------------------"
echo "Nginx and SSL setup script finished."
echo "You should now be able to access your Spring Boot application via https://rohitahuja.dev"