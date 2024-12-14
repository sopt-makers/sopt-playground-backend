#!/bin/bash

# Reload Nginx to apply port switching
reload_nginx() {
    local PORT=$1

    echo "▶️ Nginx Reload (Port switching applied) ..."

    echo "set \$service_url http://127.0.0.1:${PORT};" | sudo tee /etc/nginx/conf.d/service-url.inc
    sudo nginx -s reload
    echo "Current running Port after switching: $(sudo cat /etc/nginx/conf.d/service-url.inc)"
}