## URLs
### Getting job list
`GET` `/tasks/...`
```json[
    {
        "type":"dir",
        "name":"frontend"
    },
    {
        "type":"job",
        "name":"Deploy All"
    }
]
```

### Execute Job
`POST` `/tasks/.../execute`
returns object `ExecuteJob`

### Wait a new event
`POST` `/tasks/wait`
Client must send object `NodeInfo`. Server returns `JobDescription`. This request is long polling

### Wait until job is done
`GET` `/execution/.../join`
Wait until job is done. When job is done will return `OK` or `BAD`

### Write STDOUT
`POST` `/execution/.../stdout`
Client must send raw stdout of program

### Write STDERR
`POST` `/execution/.../stderr`
Client must send raw stderr of program

### Declare end of job
`POST` `/execution/.../finish`

### Cancel executing job
`POST` `/execution/.../cancel`

## Objects
### ExecuteJob
Information about started job
```json
{
  "buildNumber": "1",
  "path": "Deploy All"
}
```

### NodeInfo
Information about one node
```json
{
  "id": "3453535435"
}
```

### JobDescription
```json
{
  "buildNumber": "1",
  "path": "Deploy All",
  "cmd": "git clone http://example.com/project.git\n./gradlew build"
}
```