# Jogo de Batalha Naval em Rede

Este projeto implementa um jogo de **Batalha Naval** que funciona em rede, utilizando uma arquitetura cliente-servidor para comunicação entre as máquinas. O jogo permite que dois jogadores se conectem via rede local para disputar uma partida em tempo real.

## Estrutura do Jogo

- **Servidor**: Responsável por gerenciar o estado da partida, validar jogadas e sincronizar os dados entre os clientes.
- **Cliente**: Interface para o jogador fazer suas jogadas e visualizar o tabuleiro.

## Configuração
Para rodar o jogo, é necessário configurar o arquivo `config.xml`, que armazena o endereço IP para conexão.

## Como rodar

Máquina 1 (Servidor + Cliente local)
Abra o arquivo config.xml e configure o IP para o IP local da rede local da máquina.

Máquina 2 (Cliente remoto)
Abra o arquivo config.xml e configure o IP para o IP da máquina 1 (onde o servidor está rodando).

O cliente se conectará ao servidor remoto usando o IP configurado no config.xml.
