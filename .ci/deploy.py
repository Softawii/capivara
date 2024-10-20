import requests
import os

host = os.environ['PORTAINER_HOST']
endpointId = os.environ['PORTAINER_ENDPOINT_ID']
stack_name = 'capivara'
stack_file_path = './docker-compose.yaml'
access_token = os.environ['PORTAINER_ACCESS_TOKEN']

def get_envs():
    env_keys = [
        'CAPIVARA_IMAGE_TAG',
        'DISCORD_TOKEN',
        'LOG_CHANNEL_ID',
        'CURUPIRA_RESET',
        'DATABASE_DRIVER',
        'DATABASE_DIALECT',
        'DATABASE_URL',
        'DATABASE_USERNAME',
        'DATABASE_PASSWORD',
        'JAVA_ARGS',
    ]

    return [{'name': key, 'value': os.environ[key]} for key in env_keys]

def get_stack_id(stack_name):
    get_stack_res = requests.get(
        f'{host}/api/stacks',
        headers={'X-API-KEY': access_token},
        verify=False
    )
    if get_stack_res.ok:
        payload = get_stack_res.json()
        for stack in payload:
            if stack['Name'] == stack_name:
                return stack['Id']

        raise Exception('Stack not found')
    else:
        raise Exception('Failed to get stack id')

def setup_stack():
    envs = get_envs()
    with open(stack_file_path) as f:
        file_content = f.read()

        create_stack_res = requests.post(
            f'{host}/api/stacks/create/standalone/string?endpointId={endpointId}',
            json={
                "name": stack_name,
                "stackFileContent": file_content,
                "env": envs
            },
            headers={'X-API-KEY': access_token},
            verify=False
        )
        if create_stack_res.ok:
            print('Created stack with success')
            return

        if create_stack_res.status_code == 401:
            raise Exception('Unauthorized')
        
        if create_stack_res.status_code == 409:
            payload = create_stack_res.json()
            if payload['message'] == f'A stack with the normalized name \'{stack_name}\' already exists':
                update_stack(file_content, envs)
                return

            raise Exception('Unable to create and then update stack')

def update_stack(file_content, envs):
    stack_id = get_stack_id(stack_name)
    update_stack_res = requests.put(
        f'{host}/api/stacks/{stack_id}?endpointId={endpointId}',
        json={
            "stackFileContent": file_content,
            "env": envs
        },
        headers={'X-API-KEY': access_token},
        verify=False
    )

    if update_stack_res.ok:
        print('Updated stack with success')
        return
    
    if update_stack_res.status_code == 401:
        raise Exception('Unauthorized')

    raise Exception('Unable to update stack', update_stack_res.status_code)

if __name__ == '__main__':
    setup_stack()