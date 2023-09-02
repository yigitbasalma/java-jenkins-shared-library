def call(Map config) {
    config.b_config.projects.each { it ->
        def buildArgs = []

        if ( config.b_config.containsKey("buildArgs") ) {
            buildArgs.addAll(config.b_config.buildArgs)
        }

        if ( it.containsKey("buildArgs") ) {
            buildArgs.addAll(it.buildArgs)
        }

        sh """
        cd ${it.path} && \
        mvn -v && \
        mvn clean install \
            -Dproject.version=${config.project_full_version} \
            ${buildArgs.unique().join(" ")}
        """
    }
}
