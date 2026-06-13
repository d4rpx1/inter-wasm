(module
  (func (export "_start") (param i32) (result i32)
    (local i32)

    i32.const 5
    local.set 1

    local.get 0
    local.get 1
    i32.add
  )
)