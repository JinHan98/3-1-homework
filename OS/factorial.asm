.data

.text

main:
	li $v0 5
	syscall
	move $t0 $v0 
	li $t1 1
	beq $t0 1 case1
	jal fact
	move $a0 $t1
	li $v0 1
	syscall
	li $v0 10
	syscall

case1:
	li $a0 1
	li $v0 1
	syscall
	li $v0 10
	syscall




fact:
	mul $t1 $t0 $t1
	sub $t0 $t0 1
	bne $t0 1 fact
	jr $ra