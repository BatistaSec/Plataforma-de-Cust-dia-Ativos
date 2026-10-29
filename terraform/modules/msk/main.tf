resource "aws_security_group" "msk" {
  name        = "${var.project}-msk-sg-${var.environment}"
  description = "Security group for MSK Kafka"
  vpc_id      = var.vpc_id

  ingress {
    from_port       = 9092
    to_port         = 9092
    protocol        = "tcp"
    security_groups = var.allowed_sg_ids
    description     = "Kafka plaintext from ECS tasks"
  }

  ingress {
    from_port       = 9094
    to_port         = 9094
    protocol        = "tcp"
    security_groups = var.allowed_sg_ids
    description     = "Kafka TLS from ECS tasks"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.project}-msk-sg-${var.environment}" }
}

resource "aws_msk_configuration" "main" {
  name              = "${var.project}-msk-config-${var.environment}"
  kafka_versions    = [var.kafka_version]

  server_properties = <<-PROPERTIES
    auto.create.topics.enable=true
    default.replication.factor=2
    min.insync.replicas=1
    num.io.threads=8
    num.network.threads=5
    num.partitions=3
    num.replica.fetchers=2
    socket.request.max.bytes=104857600
    unclean.leader.election.enable=false
    log.retention.hours=168
  PROPERTIES
}

resource "aws_msk_cluster" "main" {
  cluster_name           = "${var.project}-kafka-${var.environment}"
  kafka_version          = var.kafka_version
  number_of_broker_nodes = var.broker_count

  broker_node_group_info {
    instance_type   = "kafka.t3.small"
    client_subnets  = slice(var.private_subnet_ids, 0, var.broker_count)
    security_groups = [aws_security_group.msk.id]

    storage_info {
      ebs_storage_info {
        volume_size = 100
      }
    }
  }

  configuration_info {
    arn      = aws_msk_configuration.main.arn
    revision = aws_msk_configuration.main.latest_revision
  }

  encryption_info {
    encryption_in_transit {
      client_broker = "PLAINTEXT"
      in_cluster    = true
    }
  }

  tags = { Name = "${var.project}-kafka-${var.environment}" }
}
