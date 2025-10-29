#!/bin/bash
# Espera o PostgreSQL ficar disponível
until pg_isready -h "${DATABASE_URL#*//}" -p 5432 -U "$DATABASE_USER"; do
  echo "Esperando banco de dados em $DATABASE_URL..."
  sleep 2
done

# Inicia a aplicação
exec java -jar target/Benucci-App-0.0.1-SNAPSHOT.jar
