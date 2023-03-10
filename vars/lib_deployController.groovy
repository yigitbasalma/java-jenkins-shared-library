def call(Map config, String sshKeyFile) {
    // SSH key file permission
    sh "chmod 600 ${sshKeyFile}"
    container_repository = config.container_artifact_repo_address

    if ( config.scope == "branch" && params.IMAGE == "" ) {
        currentBuild.result = "ABORTED"
        error("You have to set IMAGE_ID parameter for branch deployment.")
    }

    config.b_config.deploy.each { it ->
        // Replacing {environment} definition in path for backward compatibility
        path = "${it.path.replace('/{environment}', '')}/{environment}"

        if ( config.scope == "branch" ) {
            path = "${it.path}/branch/${config.target_branch}"
        }

        "${it.type}"(config, config.image, it.repo, path, it.name, sshKeyFile, container_repository)
    }
}

def argocd(Map config, String image, String repo, String path, String appName, String sshKeyFile, String containerRepository) {
    // Change image version on argocd repo and push
    sh """
    ${config.script_base}/argocd/argocd.py --image "${containerRepository}/${appName}:${image}" -r ${repo} --application-path ${path} --environment ${config.environment} --key-file "${sshKeyFile}"
    """

    // Save image version for the next environment
    if ( config.scope == "global" && config.environment_mappings.containsKey(config.environment) ) {
        sh """
        ${config.script_base}/metadata/metadata.py -p ${config.b_config.project.name} -a ${config.b_config.project.name} -e ${config.environment_mappings[config.environment]} set --image-id ${image}
        """
    }
}