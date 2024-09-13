import {Octokit} from "@octokit/rest";
import fetch from "node-fetch";

const labelName = "Waiting on Dependency PR";

async function run() {
    const octokit = new Octokit({
        auth: process.env.GITHUB_TOKEN,
        request: {
            fetch: fetch,
        },
    });

    const context = JSON.parse(process.env.GITHUB_CONTEXT);

    const pull_request = context.event.pull_request;

    const owner = context.repository_owner;
    const name = context.repository.split("/")[1];

    const prNumber = pull_request.number;
    const prBody = pull_request.body || "";

    const dependencyRegex = /## Dependencies/;
    const match = prBody.match(dependencyRegex);

    if (match) {
        const prLinks = prBody.match(/- https:\/\/github\.com\/[\w-]+\/[\w-]+\/pull\/\d+/g);

        if (prLinks && prLinks.length > 0) {
            let hasOpenDependencies = false;

            for (const link of prLinks) {
                const [, depOwner, depRepo, depNumber] = link.match(/github\.com\/([\w-]+)\/([\w-]+)\/pull\/(\d+)/);
                const {data: dependencyPr} = await octokit.pulls.get({
                    owner: depOwner,
                    repo: depRepo,
                    pull_number: depNumber,
                });

                if (dependencyPr.state === "open") {
                    hasOpenDependencies = true;
                    break;
                }
            }

            const labels = pull_request.labels.map(label => label.name);

            if (hasOpenDependencies && !labels.includes(labelName)) {
                await octokit.issues.addLabels({
                    owner,
                    repo: name,
                    issue_number: prNumber,
                    labels: [labelName],
                });
            } else if (!hasOpenDependencies && labels.includes(labelName)) {
                await octokit.issues.removeLabel({
                    owner,
                    repo: name,
                    issue_number: prNumber,
                    name: labelName,
                });
            }
        }
    }
}

run().catch(error => {
    console.error(error);
    process.exit(1);
});
