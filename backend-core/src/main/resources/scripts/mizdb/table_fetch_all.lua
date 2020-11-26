local JSON = require("JSON")

missionDatabase = missionDatabase or {}

-- get table by name
local table_name = '%s'

local db_table = missionDatabase[table_name]

if db_table then
    local data = db_table:findAll()
    local dataString = JSON:encode(data)

    db_table:deleteAll()

    return dataString
end