(module
  ;; _start(n) -> number of primes <= n
  (func (export "_start") (param $n i32) (result i32)
    (local $i i32)
    (local $count i32)
    (local $d i32)
    (local $is_prime i32)

    ;; i = 2
    i32.const 2
    local.set $i

    ;; count = 0
    i32.const 0
    local.set $count

    block $done
      loop $outer_loop
        ;; if i > n, stop
        local.get $i
        local.get $n
        i32.gt_s
        br_if $done

        ;; is_prime = 1
        i32.const 1
        local.set $is_prime

        ;; d = 2
        i32.const 2
        local.set $d

        block $check_done
          loop $inner_loop
            ;; if d * d > i, stop checking divisors
            local.get $d
            local.get $d
            i32.mul
            local.get $i
            i32.gt_s
            br_if $check_done

            ;; if i % d == 0, is_prime = 0 and stop checking
            local.get $i
            local.get $d
            i32.rem_s
            i32.eqz
            if
              i32.const 0
              local.set $is_prime
              br $check_done
            end

            ;; d = d + 1
            local.get $d
            i32.const 1
            i32.add
            local.set $d

            br $inner_loop
          end
        end

        ;; count = count + is_prime
        local.get $count
        local.get $is_prime
        i32.add
        local.set $count

        ;; i = i + 1
        local.get $i
        i32.const 1
        i32.add
        local.set $i

        br $outer_loop
      end
    end

    local.get $count
  )
)