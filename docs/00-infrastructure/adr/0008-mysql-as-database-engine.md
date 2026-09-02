# MySQL as the database engine for every service that owns data

The case study's diagram left the database choice open ("MySQL / Mongo"). We're standardizing on
MySQL for UserProfile, Authentication, and Bookmark — the three services that own relational data
per ADR on data ownership. One consistent engine means one JDBC driver/ORM pattern to learn
instead of two, and a simpler Docker Compose setup. Each service still gets its own separate
MySQL database/container — this is a shared engine choice, not a shared database instance;
services still never reach into another service's database directly.
