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
1. Deploy the JAR with `ansible-playbook deploy_frontend.yml` (Ansible is also building it).

When you are done, remove the instances, DNS settings, and key with `terraform destroy`.


Let's Debug
-----------

Prerequisite: Make sure MySQL is stopped and restart the Java app so it won't successfully come up.

1. **Heartbeat dashboard**: Site is indeed down
1. **Metricbeat system dashboard**: Check the system, which servers do you even have, what is running, the load,...
1. **Kibana monitoring**: You can also get an overview of the system here — specifically in the Beats section.
1. **Logs**: Discuss why you don't want to parse the LOG and we are using JSON instead. Filter down to `application:frontend` and `json.severity:ERROR`. You can see that something is happening with MySQL, so check if Heartbeat is collecting the data? It is, it's just not part of the dashboard.
1. **Custom visualization**: Build a custom visualization to show that MySQL is down. For example a horizontal bar, filter on `tcp.port:3306` and split the `Date histogram` on `monitor.status`. So something is up with MySQL — time to look at the logs again.
1. **Filebeat system discover**: In Filebeat filter to `fileset.module:system` and search for `mysql`. You will find an entry `Stopping MySQL Community Server...` — wondering what happened there.
1. **Auditbeat discover**: Search for `mysql` in the Auditbeat data. Find the actual command that shut down MySQL, which should be `sudo service mysql stop`. So now you know what happened and who did it.
1. Restart MySQL via SSH and then the Java application with `ansible-playbook restart_frontend.yml`, so we have the event for annotations later on.
1. Insert some data and let the audience go wild on the application.
1. **Packetbeat dashboards**: Show Overview, flows, HTTP, and MySQL.
1. **Filebeat nginx dashboard**: Show similar data as Packetbeat HTTP.
1. **Metricbeat visualization**: Show the collection of application metrics with HTTP and JMX. Visualize it in the visual builder by dividing the `average` of `jolokia.metrics.memory.heap_usage.used` with the `max` of `jolokia.metrics.memory.heap_usage.max`.
  Also annotate from the `events` index with the fields `user, application, host` and use the row template `{{user}} ran {{application}} on {{host}}`.
1. **APM**: Show the init calls — it's kind of expected that they are slow. But why is search doing so many SQL queries? David will need to fix that.
1. **SSH dashboard**: Ask if anybody tried to SSH into the machine and show the status.
1. **Alerting**: If asked about alerting, show the check for Heartbeat data (checks every minute if at least 2 pings failed in the last 5 minutes).
1. **Machine learning**: Optionally add the default nginx jobs. If the service has been running for a while you can see the anomaly when it's down. Though it doesn't use the full potential since we don't have a recurring usage pattern on the site.
