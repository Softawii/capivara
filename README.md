<div align="center">
    <img src="./docs/capybara-banner.png" alt="Banner">

[![deploy](https://github.com/Softawii/capivara/actions/workflows/deploy.yaml/badge.svg)](https://github.com/Softawii/capivara/actions/workflows/deploy.yaml)
[![build](https://github.com/Softawii/capivara/actions/workflows/build.yaml/badge.svg?branch=main)](https://github.com/Softawii/capivara/actions/workflows/build.yaml)
</div>


<h1 align="center">Capivara</h1>


Capivara é um bot para 'Discord' com o objetivo de facilitar a gerência de cargos, permissões, denúncias e muito mais! Pode chamar ele pro seu servidor [por aqui](https://discord.com/api/oauth2/authorize?client_id=983021336496590918&permissions=8&scope=bot%20applications.commands)!

### Básico da Estrutura do Bot

O bot é dividido em "setores", cada setor é responsável por um grupo de tarefas (você pode conferir cada um na nossa wiki), por exemplo:

Para o setor de manejo de packages temos:
- PackageListener
- PackageManager
- PackageService
- PackageRepository


#### Listener

Responsável por escutar os comandos vindos do discord, decodificar os campos, e, se necessário, passar a responsabilidade da regra de negócio para o Manager.

#### Manager

Reponsável por aplicar a regra de negócio e, se necessário, se comunicar com o Service (comunicando apenas com os services (poderia ser mesclado ao mesmo, porém, deixamos assim).

#### Service

Abstração para o banco, alguns tratamentos para passar para a camada de baixo

#### Repository

Bora falar com o DB?

### Para Contribuir!

Cria seu fork ou branch e faça um MR

