#!/usr/bin/env bash

set -e

repoName="Palisade"
repoId="Palisade"
artifactId="palisade"

if [ "$TRAVIS_BRANCH" == 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    git checkout master
    mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version
    POM_VERSION=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\['`
    echo "POM_VERSION = $POM_VERSION"
    if [[ "$POM_VERSION" == *SNAPSHOT ]]; then
        if [ -z "$GITHUB_TOKEN" ]; then
            echo "GITHUB_TOKEN has not been set. Please configure this in Travis CI settings"
            exit 1
        fi

        if [ -z "$RELEASE_VERSION" ]; then
            RELEASE_VERSION=${POM_VERSION%-SNAPSHOT}
        fi

        echo ""
        echo "======================================"
        echo "Tagging and releasing version $RELEASE_VERSION"
        echo "======================================"
        echo ""

        # Configure GitHub token
        git config --global credential.helper "store --file=.git/credentials"
        echo "https://${GITHUB_TOKEN}:@github.com" > .git/credentials

        # Add develop and gh-pages branches
        git remote set-branches --add origin develop gh-pages
        git pull

        echo ""
        echo "--------------------------------------"
        echo "Tagging version $RELEASE_VERSION"
        echo "--------------------------------------"
        mvn versions:set -DnewVersion=$RELEASE_VERSION -DgenerateBackupPoms=false

        git commit -a -m "prepare release $artifactId-$RELEASE_VERSION"
        git tag $artifactId-$RELEASE_VERSION
        git push origin --tags
        git push

        echo ""
        echo "--------------------------------------"
        echo "Updating doc"
        echo "--------------------------------------"
        npm install -g gitbook-cli > /dev/null 2>&1
        ./doc/scripts/buildGitbook.sh
        git checkout --orphan gh-pages
        mv _book .book
        rm -rf *
        mv .book/* .
        rm -rf .book
        rm -f doc/.gitignore
        git commit -am "Updated documentation - $RELEASE_VERSION"
        git push -u origin gh-pages -f
        git checkout master

        echo ""
        echo "--------------------------------------"
        echo "Creating GitHub release notes"
        echo "--------------------------------------"
        JSON_DATA="{
                \"tag_name\": \"$artifactId-$RELEASE_VERSION\",
                \"name\": \"$repoName $RELEASE_VERSION\",
                \"body\": \"[$RELEASE_VERSION headliners](https://github.com/gchq/$repoId/issues?q=milestone%3Av$RELEASE_VERSION+label%3Aheadliner)\n\n[$RELEASE_VERSION enhancements](https://github.com/gchq/$repoId/issues?q=milestone%3Av$RELEASE_VERSION+label%3Aenhancement)\n\n[$RELEASE_VERSION bugs fixed](https://github.com/gchq/$repoId/issues?q=milestone%3Av$RELEASE_VERSION+label%3Abug)\n\n[$RELEASE_VERSION migration notes](https://github.com/gchq/$repoId/issues?q=milestone%3Av$RELEASE_VERSION+label%3Amigration-required)\n\n[$RELEASE_VERSION all issues resolved](https://github.com/gchq/$repoId/issues?q=milestone%3Av$RELEASE_VERSION)\",
                \"draft\": false
            }"
        echo $JSON_DATA
        curl -v --data "$JSON_DATA" https://api.github.com/repos/gchq/$repoId/releases?access_token=$GITHUB_TOKEN

        echo ""
        echo "--------------------------------------"
        echo "Merging into develop and updating pom version"
        echo "--------------------------------------"
        git checkout develop
        git pull
        git merge master
        mvn release:update-versions -B
        git commit -a -m "prepare for next development iteration"
        git push
    fi
fi
