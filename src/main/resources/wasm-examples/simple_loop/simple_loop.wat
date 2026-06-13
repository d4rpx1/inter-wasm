(module
  (func (export "_start") (param $x i32) (result i32)
    (local $i i32)

    ;; i = 0
    i32.const 0
    local.set $i

    block $done
      loop $loop
        ;; if i >= 10, exit loop
        local.get $i
        i32.const 10
        i32.ge_s
        br_if $done

        ;; x = x + i
        local.get $x
        local.get $i
        i32.add
        local.set $x

        ;; i = i + 1
        local.get $i
        i32.const 1
        i32.add
        local.set $i

        ;; continue loop
        br $loop
      end
    end

    ;; return x
    local.get $x
  )
)