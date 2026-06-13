(module
  (func (export "_start") (param $n i32) (result i32)
    (local $a i32)
    (local $b i32)
    (local $i i32)
    (local $tmp i32)

    ;; a = 0
    i32.const 0
    local.set $a

    ;; b = 1
    i32.const 1
    local.set $b

    ;; i = 0
    i32.const 0
    local.set $i

    block $done
      loop $loop
        ;; if i >= n, break
        local.get $i
        local.get $n
        i32.ge_s
        br_if $done

        ;; tmp = a + b
        local.get $a
        local.get $b
        i32.add
        local.set $tmp

        ;; a = b
        local.get $b
        local.set $a

        ;; b = tmp
        local.get $tmp
        local.set $b

        ;; i = i + 1
        local.get $i
        i32.const 1
        i32.add
        local.set $i

        ;; continue loop
        br $loop
      end
    end

    ;; return a
    local.get $a
  )
)