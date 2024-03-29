def call(Map config) {
    config.b_config.projects.each { it ->
        if ( it.containsKey("registries") ) {
            it.registries.each { reg ->
                withCredentials([[$class:"UsernamePasswordMultiBinding", credentialsId: reg.id, usernameVariable: "USERNAME", passwordVariable: "PASSWORD"]]) {
                    sh """
                    find ${WORKSPACE} -name "*.jar" -exec \
                        curl -s -u ${USERNAME}:${PASSWORD} \
                        -X POST "${reg.url}/service/rest/v1/components?repository=${reg.repo}" \
                        -F "maven2.asset1=@${it.path}/pom.xml" \
                        -F "maven2.asset1.extension=pom" \
                        -F "maven2.asset2=@{}" \
                        -F maven2.asset2.extension=jar \
                        -F maven2.generate-pom=false \\;
                    """
                }
            }
        }
    }
}