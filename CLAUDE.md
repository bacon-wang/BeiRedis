## Agent skills

### Issue tracker

Issues live in GitHub Issues (bacon-wang/BeiRedis), managed with the `gh` CLI. See `docs/agents/issue-tracker.md`.

### Triage labels

Default vocabulary: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: root `GLOSSARY.md` + `docs/adr/`. See `docs/agents/domain.md`.

### Ticket 工作流

只在项目根目录的 `main` 分支上工作：不建分支，不用 worktree。

`ready-for-human` 的 ticket 由用户实现，agent 负责学习包、骨架、测试，不写用户要写的实现。口令（"开始 #N" / "检查" / "提示" / "完成 #N"）和步骤见 `docs/agents/ticket-workflow.md`。跑测试：`scripts/run-tests.sh`。
