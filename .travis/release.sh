#!/usr/bin/env bash
set -o errexit -o nounset

USERNAME="Dwolla Bot"
RELEASE_BRANCH="main"

commit_username=$(git log -n1 --format=format:"%an")
if [[ "$commit_username" == "$USERNAME" ]]; then
  echo "Refusing to release a commit created by this script."
  exit 0
fi

if [ "$TRAVIS_BRANCH" != "${RELEASE_BRANCH}" ]; then
  echo "Only the ${RELEASE_BRANCH} branch will be released. This branch is $TRAVIS_BRANCH."
  exit 0
fi

git config user.name "$USERNAME"
git config user.email "dev+dwolla-bot@dwolla.com"

git remote add release "https://$GH_TOKEN@github.com/Dwolla/anorm-cats.git"
git fetch release

git clean -dxf
git checkout ${RELEASE_BRANCH}
git branch --set-upstream-to=release/${RELEASE_BRANCH}

MAIN_BRANCH=$(git rev-parse HEAD)
if [ "$TRAVIS_COMMIT" != "$MAIN_BRANCH" ]; then
  echo "Checking out ${RELEASE_BRANCH} set HEAD to $MAIN_BRANCH, but Travis was building $TRAVIS_COMMIT, so refusing to continue."
  exit 0
fi

sbt clean "release with-defaults"
