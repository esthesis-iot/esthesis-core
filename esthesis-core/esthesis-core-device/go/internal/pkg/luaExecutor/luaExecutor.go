package luaExecutor

import (
	log "github.com/sirupsen/logrus"
	"github.com/yuin/gopher-lua"
)

func ExecuteLuaScript(payload string, luaScriptFileLocation string) string {
	L := lua.NewState()
	L.SetGlobal("payload", lua.LString(payload))
	defer L.Close()
	if err := L.DoFile(luaScriptFileLocation); err != nil {
		log.WithError(err).Error("Error executing lua script.")
		return payload
	}

	lv := L.Get(-1)
	if str, ok := lv.(lua.LString); ok {
		return str.String()
	} else {
		log.Error("Value returned from LUA script is not a string.")
		return payload
	}
}
