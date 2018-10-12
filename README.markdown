Monitor Your Legacy Application
================================

Lightsail Setup
---------------

Make sure you have run this before the demo, because some steps take time and require a decent internet connection.

1. Make sure you have your AWS account set up, access key created, and added as environment variables in `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`. Protip: Use [https://github.com/sorah/envchain](https://github.com/sorah/envchain) to keep your environment variables safe.
1. Create the Elastic Cloud instance with the same version as specified in *variables.yml*'s `elastic_version`, enable Kibana as well as the GeoIP & user agent plugins, and set the environment variables with the values for `ELASTICSEARCH_HOST`, `ELASTICSEARCH_USER`, `ELASTICSEARCH_PASSWORD`, `KIBANA_HOST`, `KIBANA_ID`, `MYSQL_USER`, and `MYSQL_PASSWORD`.
1. Change into the *lightsail/* directory.
1. Change the settings to a domain you have registered under Route53 in *inventory*, *variables.tf*, and *variables.yml*. Set the Hosted Zone for that domain and export the Zone ID under the environment variable `TF_VAR_zone_id`. If you haven't created the Hosted Zone yet, you should set it up in the AWS Console first and then set the environment variable.
1. If you haven't installed the AWS plugin for Terraform, get it with `terraform init` first. Then create the keypair, DNS settings, and instances with `terraform apply`.
1. Open HTTPS on the network configuration on the frontend and monitor instances, MySQL on the backend instance, and TCP 8200 on the monitoring instance (waiting for this [Terraform issue](https://github.com/terraform-providers/terraform-provider-aws/issues/700)).
1. Apply the base configuration to all instances with `ansible-playbook configure_all.yml`.
1. Apply the instance specific configurations with `ansible-playbook configure_backend.yml` and `ansible-playbook configure_monitor.yml`.
1. Deploy the JARs with `ansible-playbook deploy_bad.yml`, `ansible-playbook deploy_backend.yml`, and `ansible-playbook deploy_frontend.yml` (Ansible is also building them).

When you are done, remove the instances, DNS settings, and key with `terraform destroy`.


Let's Debug
-----------

Prerequisite: Make sure MySQL is stopped and restart the Java app so it won't successfully come up.

1. Heartbeat dashboard: Site is indeed down
1. Metricbeat system dashboard: Check the system, which servers do you even have what is running, and the load.
1. You can also get a bit of an overview on the Monitoring page.
1. Logs: Why not .log and .json instead, filter down to `application:java` and `json.severity:ERROR`.
1. Heartbeat: MySQL data on visualizations with a filter on `tcp.port:3306` and then split on `monitor.status`.
1. Restart MySQL via SSH and then the Java application with `ansible-playbook restart_frontend.yml`, so we have the event for annotations later on.
1. Insert some data and let the audience go wild on the application. Show the results in Packetbeat and then Filebeat modules nginx.
1. Application metrics with HTTP and JMX plus annotations in TSVB.
1. APM: Show the init calls — that's not unexpected. But why is search doing so many calls? David will need to fix that.
1. SSH dashboard, Auditbeat,...
