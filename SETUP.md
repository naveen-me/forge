# Agent Handoff / Setup

## Repository handoff

1. Create a new Git repository.
2. Extract this project-brain package into the repository root.
3. Commit the files.
4. Connect the repository to your autonomous coding agent.
5. Give it `MASTER_PROMPT.md` as the initial task prompt.

Jules automatically looks for `AGENTS.md` in the repository root. It can clone the repo, install dependencies in its Ubuntu VM, modify files and run tests. The master prompt is included so the same project can be handed to other coding agents too.

## Recommended first prompt

Copy the contents of `MASTER_PROMPT.md` into the agent's initial task prompt, or simply use:

"Read MASTER_PROMPT.md and all referenced project documents. You are the autonomous engineering owner of this repository. Execute the project end-to-end according to those documents. Start with the mandatory WPE -> frame/buffer -> GPAC CPU compositor POC. Research upstream APIs yourself, implement, test, benchmark, document evidence, and continue through the roadmap. Do not use screenshot-per-frame HTML rendering. Do not silently replace approved architecture. Do not ask me to relay routine technical questions. Only stop for a genuine architecture blocker or missing external credential/resource."

## Important

Do not give the agent a vague prompt such as:

"Build a playout engine."

The repository documents are the persistent project context.
