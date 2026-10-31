<?php

namespace App\Exceptions;

use RuntimeException;

class ApiProblem extends RuntimeException
{
    public function __construct(public string $errorCode, public int $status = 409, public array $details = [])
    {
        parent::__construct($errorCode);
    }
}
